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
package io.tesla.maven.bridge.support.artifact;

import io.tesla.maven.bridge.MavenArtifactResolver;
import io.tesla.maven.bridge.support.MavenSettings;
import io.tesla.maven.bridge.support.MavenSettingsFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.inject.Nullable;

@Named("remote-artifact-resolver-using-settings")
@Singleton
public class RemoteMavenArtifactResolverUsingSettings extends RemoteMavenArtifactResolver implements MavenArtifactResolver {

  private final MavenSettingsFactory mavenSettingsFactory;

  public RemoteMavenArtifactResolverUsingSettings(final ServiceLocator serviceLocator, final MavenSettingsFactory mavenSettingsFactory) {
    this(serviceLocator, mavenSettingsFactory, NO_SESSION_PROVIDER);
  }

  @Inject
  public RemoteMavenArtifactResolverUsingSettings(final ServiceLocator serviceLocator, final MavenSettingsFactory mavenSettingsFactory,
      final @Nullable Provider<RepositorySystemSession> sessionProvider) {
    super(serviceLocator, sessionProvider);
    this.mavenSettingsFactory = mavenSettingsFactory;
  }

  @Override
  protected Artifact doResolve(final ArtifactRequest artifactRequest, final RepositorySystemSession session, final RemoteRepository... repositories) throws ArtifactResolutionException {
    final MavenSettings mavenSettings = mavenSettingsFactory.create();
    return super.doResolve(mavenSettings.inject(artifactRequest), mavenSettings.inject(session), repositories);
  }

}
