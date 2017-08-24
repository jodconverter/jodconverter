/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * {@link OfficeManager} pool implementation that does not depend on an office installation to
 * process conversion taks.
 */
public final class SimpleOfficeManager extends OfficeManagerPool {

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link SimpleOfficeManager} with default configuration.
   *
   * @return A {@link SimpleOfficeManager} with default configuration.
   */
  public static SimpleOfficeManager make() {
    return builder().build();
  }

  /**
   * Creates a new {@link SimpleOfficeManager} with default configuration. The created manager will
   * then be the unique instance of the {@link InstalledOfficeManagerHolder} class. Note that if the
   * {@code InstalledOfficeManagerHolder} class already holds an {@code OfficeManager} instance, the
   * owner of this existing manager is responsible to stopped it.
   *
   * @return A {@link SimpleOfficeManager} with default configuration.
   */
  public static SimpleOfficeManager install() {
    return builder().install().build();
  }

  private SimpleOfficeManager(final int poolSize, final SimpleOfficeManagerPoolConfig config) {
    super(poolSize, config);
  }

  /**
   * A builder for constructing a {@link SimpleOfficeManager}.
   *
   * @see SimpleOfficeManager
   */
  public static final class Builder {

    /** The default size of the pool. */
    public static final int DEFAULT_POOL_SIZE = 1;

    /** The maximum size of the pool. */
    public static final int MAX_POOL_SIZE = 1000;

    private boolean install;
    private int poolSize = DEFAULT_POOL_SIZE;
    private File workingDir;
    private long taskExecutionTimeout = OfficeManagerPoolEntryConfig.DEFAULT_TASK_EXECUTION_TIMEOUT;
    private long taskQueueTimeout = OfficeManagerPoolConfig.DEFAULT_TASK_QUEUE_TIMEOUT;

    // Private ctor so only DefaultOfficeManager can initialize an instance of this builder.
    private Builder() {
      super();
    }

    /**
     * Creates the converter that is specified by this builder.
     *
     * @return The converter that is specified by this builder.
     */
    public SimpleOfficeManager build() {

      if (workingDir == null) {
        workingDir = new File(System.getProperty("java.io.tmpdir"));
      }

      final SimpleOfficeManagerPoolConfig config = new SimpleOfficeManagerPoolConfig(workingDir);
      config.setTaskExecutionTimeout(taskExecutionTimeout);
      config.setTaskQueueTimeout(taskQueueTimeout);

      final SimpleOfficeManager manager = new SimpleOfficeManager(poolSize, config);
      if (install) {
        InstalledOfficeManagerHolder.setInstance(manager);
      }
      return manager;
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
    public Builder install() {

      this.install = true;
      return this;
    }

    /**
     * Specifies the pool size of the manager.
     *
     * @param poolSize The pool size.
     * @return This builder instance.
     */
    public Builder poolSize(final int poolSize) {

      Validate.inclusiveBetween(
          0,
          MAX_POOL_SIZE,
          poolSize,
          String.format("The poolSize %s must be between %d and %d", poolSize, 1, MAX_POOL_SIZE));
      this.poolSize = poolSize;
      return this;
    }

    /**
     * Specifies the directory where temporary office profile directories will be created. An office
     * profile directory is created per office process launched.
     *
     * <p>&nbsp; <b><i>Default</i></b>: The system temporary directory as specified by the <code>
     * java.io.tmpdir</code> system property.
     *
     * @param workingDir The new working directory to set.
     * @return This builder instance.
     */
    public Builder workingDir(final File workingDir) {

      this.workingDir = workingDir;
      return this;
    }

    /**
     * Specifies the directory where temporary office profile directories will be created. An office
     * profile directory is created per office process launched.
     *
     * <p>&nbsp; <b><i>Default</i></b>: The system temporary directory as specified by the <code>
     * java.io.tmpdir</code> system property.
     *
     * @param workingDir The new working directory to set.
     * @return This builder instance.
     */
    public Builder workingDir(final String workingDir) {

      return StringUtils.isBlank(workingDir) ? this : workingDir(new File(workingDir));
    }

    /**
     * Specifies the maximum time allowed to process a task. If the processing time of a task is
     * longer than this timeout, this task will be aborted and the next task is processed.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
     *
     * @param taskExecutionTimeout The task execution timeout, in milliseconds.
     * @return This builder instance.
     */
    public Builder taskExecutionTimeout(final long taskExecutionTimeout) {

      Validate.inclusiveBetween(
          0,
          Long.MAX_VALUE,
          taskExecutionTimeout,
          String.format(
              "The taskExecutionTimeout %s must greater than or equal to 0", taskExecutionTimeout));
      this.taskExecutionTimeout = taskExecutionTimeout;
      return this;
    }

    /**
     * Specifies the maximum living time of a task in the conversion queue. The task will be removed
     * from the queue if the waiting time is longer than this timeout.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 30000 (30 seconds)
     *
     * @param taskQueueTimeout The task queue timeout, in milliseconds.
     * @return This builder instance.
     */
    public Builder taskQueueTimeout(final long taskQueueTimeout) {

      Validate.inclusiveBetween(
          0,
          Long.MAX_VALUE,
          taskQueueTimeout,
          String.format(
              "The taskQueueTimeout %s must greater than or equal to 0", taskQueueTimeout));
      this.taskQueueTimeout = taskQueueTimeout;
      return this;
    }
  }
}
