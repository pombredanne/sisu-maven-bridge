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
package io.tesla.maven.bridge.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ComponentSupport {

  private Logger log;

  protected Logger log() {
    if (log == null) {
      log = LoggerFactory.getLogger(this.getClass());
    }
    return log;
  }

  protected static <T> T assertNotNull(final T object, final Object message) {
    if (object == null) {
      throw new NullPointerException(message.toString());
    }
    return object;
  }

}
