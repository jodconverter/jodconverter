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

package org.jodconverter.core.office;

import java.io.File;
import java.io.IOException;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.util.FileUtils;

/** Provides helper functions for office. */
public final class OfficeUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeUtils.class);

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

  /**
   * Deletes a file with a fallback (renaming) on deletion failure. If the file is a directory,
   * delete it and all subdirectories.
   *
   * @param file File or directory to delete, can be {@code null}.
   * @param interval The interval between each deletion attempt.
   * @param timeout The timeout after which we won't try again to execute the deletion.
   */
  public static void deleteOrRenameFile(
      final @NonNull File file, final long interval, final long timeout) {

    if (!file.exists()) {
      return;
    }

    LOGGER.debug("Deleting '{}'", file);
    try {
      final DeleteFileRetryable retryable = new DeleteFileRetryable(file);
      retryable.execute(interval, timeout);
    } catch (RetryTimeoutException deleteEx) {
      final File oldFile =
          new File(file.getParentFile(), file.getName() + ".old." + System.currentTimeMillis());
      if (file.renameTo(oldFile)) {
        if (LOGGER.isWarnEnabled()) {
          LOGGER.warn("Could not delete '" + file + "'; renamed it to '" + oldFile + "'", deleteEx);
        }
      } else {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error("Could not delete '" + file + "'", deleteEx);
        }
      }
    }
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private OfficeUtils() {
    super();

    throw new AssertionError("Utility class must not be instantiated");
  }

  /** Delete a file or directory (retryable). */
  private static class DeleteFileRetryable extends AbstractRetryable<RuntimeException> {
    private final File file;

    /**
     * Creates a new instance of the class for the specified file.
     *
     * @param file The file to delete.
     */
    private DeleteFileRetryable(final File file) {
      super();

      this.file = file;
    }

    @Override
    protected void attempt() throws TemporaryException {

      try {
        // Try to delete the file
        FileUtils.delete(file);
        if (file.exists()) {
          throw new TemporaryException("The file still exists");
        }
      } catch (IOException ex) {
        // Throw a TemporaryException
        throw new TemporaryException(ex);
      }
    }
  }
}
