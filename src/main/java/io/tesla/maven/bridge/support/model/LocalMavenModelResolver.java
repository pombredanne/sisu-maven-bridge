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
import io.tesla.maven.bridge.support.artifact.LocalMavenArtifactResolver;
import io.tesla.maven.bridge.support.model.internal.MavenModelResolverSupport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;


@Named("local-model-resolver")
@Singleton
public class LocalMavenModelResolver extends MavenModelResolverSupport implements MavenModelResolver {

  @Inject
  public LocalMavenModelResolver(final LocalMavenArtifactResolver artifactResolver) {
    super(artifactResolver);
  }

}
