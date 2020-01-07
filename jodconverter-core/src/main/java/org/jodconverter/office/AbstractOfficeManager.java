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

package org.jodconverter.office;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Base class for all {@link OfficeManager}. */
abstract class AbstractOfficeManager implements OfficeManager, TemporaryFileMaker {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOfficeManager.class);

  protected final OfficeManagerConfig config;
  private final AtomicLong tempFileCounter;
  private File tempDir;

  /**
   * Creates a temporary directory under the specified directory.
   *
   * @param workingDir The directory under which to create a temp directory.
   * @return The created directory.
   */
  protected static File makeTempDir(final File workingDir) {

    final File tempDir = new File(workingDir, "jodconverter_" + UUID.randomUUID().toString());
    tempDir.mkdir();
    if (!tempDir.isDirectory()) {
      throw new IllegalStateException(String.format("Cannot create temp directory: %s", tempDir));
    }
    return tempDir;
  }

  /** Constructs a new instance of the class with the specified settings. */
  protected AbstractOfficeManager(final OfficeManagerConfig config) {
    super();

    this.config = config;

    // Initialize the temp file counter
    tempFileCounter = new AtomicLong(0);
  }

  @Override
  public File makeTemporaryFile() {
    return new File(tempDir, "tempfile_" + tempFileCounter.getAndIncrement());
  }

  @Override
  public File makeTemporaryFile(final String extension) {
    return new File(tempDir, "tempfile_" + tempFileCounter.getAndIncrement() + "." + extension);
  }

  /** Makes the temporary directory. */
  protected void makeTempDir() {

    deleteTempDir();
    tempDir = makeTempDir(config.getWorkingDir());
  }

  /** Deletes the temporary directory. */
  protected void deleteTempDir() {

    if (tempDir != null) {
      LOGGER.debug("Deleting temporary directory '{}'", tempDir);
      try {
        FileUtils.deleteDirectory(tempDir);
      } catch (IOException ioEx) {
        LOGGER.error("Could not temporary profileDir: {}", ioEx.getMessage());
      }
    }
  }

  /**
   * A builder for constructing an {@link AbstractOfficeManager}.
   *
   * @see AbstractOfficeManager
   */
  @SuppressWarnings("unchecked")
  public abstract static class AbstractOfficeManagerBuilder<
      B extends AbstractOfficeManagerBuilder<B>> {

    protected boolean install;
    protected File workingDir;

    // Protected ctor so only subclasses can initialize an instance of this builder.
    protected AbstractOfficeManagerBuilder() {
      super();
    }

    /**
     * Specifies whether the office manager that will be created by this builder will then set the
     * unique instance of the {@link InstalledOfficeManagerHolder} class. Note that if the {@code
     * InstalledOfficeManagerHolder} class already holds an {@code OfficeManager} instance, the
     * owner of this existing manager is responsible to stopped it.
     *
     * <p>&nbsp; <b><i>Default</i></b>: false
     *
     * @return This builder instance.
     */
    public B install() {

      this.install = true;
      return (B) this;
    }

    /**
     * Specifies the directory where temporary files and directories are created.
     *
     * <p>&nbsp; <b><i>Default</i></b>: The system temporary directory as specified by the <code>
     * java.io.tmpdir</code> system property.
     *
     * @param workingDir The new working directory to set.
     * @return This builder instance.
     */
    public B workingDir(final File workingDir) {

      this.workingDir = workingDir;
      return (B) this;
    }

    /**
     * Specifies the directory where temporary files and directories are created.
     *
     * <p>&nbsp; <b><i>Default</i></b>: The system temporary directory as specified by the <code>
     * java.io.tmpdir</code> system property.
     *
     * @param workingDir The new working directory to set.
     * @return This builder instance.
     */
    public B workingDir(final String workingDir) {

      return StringUtils.isBlank(workingDir) ? (B) this : workingDir(new File(workingDir));
    }

    /**
     * Creates the manager that is specified by this builder.
     *
     * @return The manager that is specified by this builder.
     */
    protected abstract AbstractOfficeManager build();
  }
}
