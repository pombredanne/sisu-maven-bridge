/*
 * Copyright (c) 2009-2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 */
package io.tesla.maven.bridge.support.settings;

import io.tesla.maven.bridge.internal.RepositorySystemSessionWrapper;
import io.tesla.maven.bridge.support.MavenSettings;
import io.tesla.maven.bridge.support.RemoteRepositoryBuilder;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.AuthenticationSelector;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.MirrorSelector;
import org.sonatype.aether.repository.ProxySelector;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.util.repository.DefaultAuthenticationSelector;
import org.sonatype.aether.util.repository.DefaultMirrorSelector;
import org.sonatype.aether.util.repository.DefaultProxySelector;
import com.google.common.collect.Maps;

/**
 * TODO
 *
 * @since 2.0
 */
public class DefaultMavenSettings implements MavenSettings {

  private MirrorSelector mirrorSelector;

  private Collection<RemoteRepository> repositories;

  private AuthenticationSelector authenticationSelector;

  private ProxySelector proxySelector;

  private LocalRepositoryManager localRepositoryManager;

  private RepositorySystem repositorySystem;

  public DefaultMavenSettings(final File globalSettings, final File userSettings, final RepositorySystem repositorySystem, final List<RemoteRepository> repositories) {
    this.repositorySystem = repositorySystem;
    final SettingsBuildingRequest settingsRequest = new DefaultSettingsBuildingRequest();

    settingsRequest.setGlobalSettingsFile(globalSettings);
    settingsRequest.setUserSettingsFile(userSettings);
    settingsRequest.setSystemProperties(System.getProperties());

    // TODO shall we set custom properties?

    try {
      final SettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();
      Settings settings = settingsBuilder.build(settingsRequest).getEffectiveSettings();

      this.repositories = getRepositories(settings, repositories);
      this.mirrorSelector = createMirrorSelector(settings);
      this.authenticationSelector = createAuthenticationSelector(settings);
      this.proxySelector = createProxySelector(settings);
      this.localRepositoryManager = createLocalRepositoryManager(settings);
    } catch (SettingsBuildingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public ArtifactRequest inject(final ArtifactRequest request) {
    for (RemoteRepository repository : repositories) {
      request.addRepository(repository);
    }
    return request;
  }

  @Override
  public CollectRequest inject(final CollectRequest request) {
    for (RemoteRepository repository : repositories) {
      request.addRepository(repository);
    }
    return request;
  }

  @Override
  public RepositorySystemSession inject(final RepositorySystemSession session) {
    return new RepositorySystemSessionWrapper(session) {

      public MirrorSelector getMirrorSelector() {
        return new MirrorSelector() {
          @Override
          public RemoteRepository getMirror(final RemoteRepository repository) {
            if (mirrorSelector != null) {
              final RemoteRepository mirror = mirrorSelector.getMirror(repository);
              if (mirror != null) {
                return mirror;
              }
            }
            return session.getMirrorSelector().getMirror(repository);
          }
        };
      }

      public AuthenticationSelector getAuthenticationSelector() {
        return new AuthenticationSelector() {
          @Override
          public Authentication getAuthentication(final RemoteRepository repository) {
            if (authenticationSelector != null) {
              final Authentication auth = authenticationSelector.getAuthentication(repository);
              if (auth != null) {
                return auth;
              }
            }
            return session.getAuthenticationSelector().getAuthentication(repository);
          }
        };
      }

      public ProxySelector getProxySelector() {
        return new ProxySelector() {
          @Override
          public org.sonatype.aether.repository.Proxy getProxy(final RemoteRepository repository) {
            if (proxySelector != null) {
              final org.sonatype.aether.repository.Proxy proxy = proxySelector.getProxy(repository);
              if (proxy != null) {
                return proxy;
              }
            }
            return session.getProxySelector().getProxy(repository);
          }
        };
      }

      public LocalRepositoryManager getLocalRepositoryManager() {
        final LocalRepositoryManager sessionLocalRepositoryManager = session.getLocalRepositoryManager();
        if (sessionLocalRepositoryManager == null) {
          return localRepositoryManager;
        }
        return sessionLocalRepositoryManager;
      }

      public LocalRepository getLocalRepository() {
        final LocalRepositoryManager manager = getLocalRepositoryManager();
        LocalRepository repository = null;
        if (manager != null) {
          repository = manager.getRepository();
          if (repository == null && localRepositoryManager != null) {
            return localRepositoryManager.getRepository();
          }
        }
        return repository;
      }
    };
  }

  private static Collection<RemoteRepository> getRepositories(final Settings settings, final List<RemoteRepository> repositories) {
    final Map<String, RemoteRepository> remoteRepositories = Maps.newHashMap();

    final List<String> activeProfiles = settings.getActiveProfiles();
    for (Profile profile : settings.getProfiles()) {
      if (activeProfiles.contains(profile.getId())) {
        for (Repository repository : profile.getRepositories()) {
          remoteRepositories.put(repository.getId(), RemoteRepositoryBuilder.remoteRepository(repository));
        }
      }
    }
    if (repositories != null && !repositories.isEmpty()) {
      for (final RemoteRepository repository : repositories) {
        remoteRepositories.put(repository.getId(), repository);
      }
    }
    if (!remoteRepositories.containsKey("central")) {
      final RemoteRepository central = RemoteRepositoryBuilder.remoteRepository("central", "default", "http://repo.maven.apache.org/maven2");
      remoteRepositories.put("central", central);
    }

    return remoteRepositories.values();
  }

  private static MirrorSelector createMirrorSelector(final Settings settings) {
    final List<Mirror> mirrors = settings.getMirrors();
    if (mirrors != null && mirrors.size() > 0) {
      final DefaultMirrorSelector mirrorSelector = new DefaultMirrorSelector();
      for (Mirror mirror : mirrors) {
        mirrorSelector.add(mirror.getId(), mirror.getUrl(), mirror.getLayout(), false, mirror.getMirrorOf(), mirror.getMirrorOfLayouts());
      }
      return mirrorSelector;
    }
    return null;
  }

  private AuthenticationSelector createAuthenticationSelector(final Settings settings) {
    final List<Server> servers = settings.getServers();
    if (servers != null && servers.size() > 0) {
      final DefaultAuthenticationSelector authenticationSelector = new DefaultAuthenticationSelector();
      for (Server server : servers) {
        authenticationSelector.add(server.getId(), new Authentication(server.getUsername(), server.getPassword(), server.getPrivateKey(), server.getPassphrase()));
      }
      return authenticationSelector;
    }
    return null;
  }

  private ProxySelector createProxySelector(final Settings settings) {
    final List<org.apache.maven.settings.Proxy> proxies = settings.getProxies();
    if (proxies != null && proxies.size() > 0) {
      final DefaultProxySelector ps = new DefaultProxySelector();
      for (org.apache.maven.settings.Proxy proxy : proxies) {
        // proxies might be present but deactivated
        // but for bridge resolution they would be still picked up and used
        if (proxy.isActive()) {
          ps.add(new org.sonatype.aether.repository.Proxy(proxy.getProtocol(), proxy.getHost(), proxy.getPort(), new Authentication(proxy.getUsername(), proxy.getPassword())),
              proxy.getNonProxyHosts());
        }
      }
      return ps;
    }
    return null;
  }

  private LocalRepositoryManager createLocalRepositoryManager(final Settings settings) {
    final String localRepositoryPath = settings.getLocalRepository();
    if (localRepositoryPath == null) {
      return null;
    }

    return repositorySystem.newLocalRepositoryManager(new LocalRepository(localRepositoryPath));
  }

}
