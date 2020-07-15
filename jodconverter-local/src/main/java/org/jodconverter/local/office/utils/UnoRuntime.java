/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jodconverter.local.office.utils;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.util.AssertUtils;

/**
 * The purpose of this class is to provide a single access point to the {@link
 * com.sun.star.uno.UnoRuntime} class, making the mocking of the class possible.
 */
public class UnoRuntime {

  private static UnoRuntime instance = new UnoRuntime();

  /**
   * Gets the default {@link UnoRuntime} instance.
   *
   * @return The default {@link UnoRuntime}.
   */
  public static @NonNull UnoRuntime getInstance() {
    return instance;
  }

  /**
   * Sets the default {@link UnoRuntime} instance.
   *
   * @param unoRuntime The default {@link UnoRuntime}.
   */
  public static void setInstance(final @NonNull UnoRuntime unoRuntime) {
    AssertUtils.notNull(unoRuntime, "uno must not be null");
    synchronized (UnoRuntime.class) {
      instance = unoRuntime;
    }
  }

  /**
   * Queries the given UNO object for the given Java class (which must represent a UNO interface
   * type).
   *
   * @param zInterface A Java class representing a UNO interface type.
   * @param object A reference to any Java object representing (a facet of) a UNO object; may be
   *     <code>null</code>.
   * @return A reference to the requested UNO interface type if available, otherwise <code>null
   *     </code>.
   * @param <T> The Java class representing the UNO interface type.
   * @see com.sun.star.uno.UnoRuntime#queryInterface(Class, Object)
   */
  public <T> T queryInterface(Class<T> zInterface, Object object) {
    return com.sun.star.uno.UnoRuntime.queryInterface(zInterface, object);
  }
}
