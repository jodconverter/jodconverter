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

package org.jodconverter.core.office;

import java.io.File;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Provides helper functions for office. */
public final class OfficeUtils {

  /**
   * Gets the default working directory, which is the java.io.tmpdir system property.
   *
   * @return A {@code File} instance that is default working directory.
   */
  public static @NonNull File getDefaultWorkingDir() {
    return new File(System.getProperty("java.io.tmpdir"));
  }

  /**
   * Validates that the specified File instance is a valid working directory. To be valid, a working
   * directory must be a writable, existing directory.
   *
   * @param workingDir The directory to validate.
   * @exception IllegalStateException If the specified directory if not a valid working directory.
   */
  public static void validateWorkingDir(final @NonNull File workingDir) {

    if (!workingDir.isDirectory()) {
      throw new IllegalStateException(
          "workingDir doesn't exist or is not a directory: " + workingDir);
    }

    if (!workingDir.canWrite()) {
      throw new IllegalStateException(
          String.format("workingDir '%s' cannot be written to", workingDir));
    }
  }

  /**
   * Stops an <code>OfficeManager</code> unconditionally.
   *
   * <p>Equivalent to {@link OfficeManager#stop()}, except any exceptions will be ignored. This is
   * typically used in finally blocks.
   *
   * <p>Example code:
   *
   * <pre>
   * OfficeManager manager = null;
   * try {
   *     manager = LocalOfficeManager().make();
   *     manager.start();
   *
   *     // process manager
   *
   * } catch (Exception e) {
   *     // error handling
   * } finally {
   *     OfficeUtils.stopQuietly(manager);
   * }
   * </pre>
   *
   * @param manager the manager to stop, may be null or already stopped.
   */
  public static void stopQuietly(final @Nullable OfficeManager manager) {

    try {
      if (manager != null) {
        manager.stop();
      }
    } catch (final OfficeException ignored) {
      // ignored
    }
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private OfficeUtils() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
