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
package io.tesla.maven.bridge.support.model;

import io.tesla.maven.bridge.MavenModelResolver;
import io.tesla.maven.bridge.support.artifact.RemoteMavenArtifactResolver;
import io.tesla.maven.bridge.support.model.internal.MavenModelResolverSupport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.inject.Nullable;

@Named("remote-model-resolver")
@Singleton
public class RemoteMavenModelResolver extends MavenModelResolverSupport implements MavenModelResolver {

  public RemoteMavenModelResolver(final RemoteMavenArtifactResolver artifactResolver) {
    super(artifactResolver);
  }

  @Inject
  public RemoteMavenModelResolver(final RemoteMavenArtifactResolver artifactResolver, final @Nullable Provider<RepositorySystemSession> sessionProvider) {
    super(artifactResolver, sessionProvider);
  }

}
