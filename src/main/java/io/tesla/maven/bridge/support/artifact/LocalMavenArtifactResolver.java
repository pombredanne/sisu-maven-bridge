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

import static io.tesla.maven.bridge.Names.LOCAL_ARTIFACT_RESOLVER_ROOT_DIR;

import io.tesla.maven.bridge.MavenArtifactResolver;
import io.tesla.maven.bridge.support.artifact.internal.LocalMavenArtifactResolverSupport;

import java.io.File;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;


@Named("local-artifact-resolver")
@Singleton
public class LocalMavenArtifactResolver extends LocalMavenArtifactResolverSupport implements MavenArtifactResolver {

  private final File basedir;

  @Inject
  public LocalMavenArtifactResolver(final @Named(LOCAL_ARTIFACT_RESOLVER_ROOT_DIR) File basedir) {
    this.basedir = assertNotNull(basedir, "Repository base directory not specified");
  }

  @Override
  protected File getBaseDir() {
    return basedir;
  }

}
