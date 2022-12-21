/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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

package org.jodconverter.core.test.util;

/** Contains helper functions while testing. */
public final class TestUtil {

  /**
   * Causes the currently executing thread to sleep (temporarily cease execution) for the specified
   * number of milliseconds. InterruptedException exception are ignored.
   *
   * @param millisec the length of time to sleep in milliseconds.
   */
  public static void sleepQuietly(final long millisec) {
    try {
      Thread.sleep(millisec);
    } catch (InterruptedException ignore) {
      // ignore
    }
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private TestUtil() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
