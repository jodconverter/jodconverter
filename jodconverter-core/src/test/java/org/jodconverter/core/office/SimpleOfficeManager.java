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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jodconverter.core.util.AssertUtils;

/**
 * {@link OfficeManager} pool implementation that does not depend on an office installation to
 * process conversion tasks.
 */
public final class SimpleOfficeManager extends AbstractOfficeManagerPool {

  // The default size of the pool
  private static final int DEFAULT_POOL_SIZE = 1;
  // The maximum size of the pool.
  private static final int MAX_POOL_SIZE = 1000;

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

  private SimpleOfficeManager(
      final File workingDir,
      final int poolSize,
      final Long taskExecutionTimeout,
      final Long taskQueueTimeout) {
    super(workingDir, poolSize, taskQueueTimeout);

    setEntries(
        IntStream.range(0, poolSize)
            .mapToObj(i -> new SimpleOfficeManagerPoolEntry(taskExecutionTimeout))
            .collect(Collectors.toList()));
  }

  /**
   * A builder for constructing a {@link SimpleOfficeManager}.
   *
   * @see SimpleOfficeManager
   */
  public static final class Builder extends AbstractOfficeManagerPoolBuilder<Builder> {

    private Integer poolSize;

    // Private constructor so only SimpleOfficeManager can initialize an instance of this builder.
    private Builder() {
      super();
    }

    @Override
    public SimpleOfficeManager build() {

      if (workingDir == null) {
        workingDir = OfficeUtils.getDefaultWorkingDir();
      }

      final SimpleOfficeManager manager =
          new SimpleOfficeManager(
              workingDir,
              poolSize == null ? DEFAULT_POOL_SIZE : poolSize,
              taskExecutionTimeout,
              taskQueueTimeout);
      if (install) {
        InstalledOfficeManagerHolder.setInstance(manager);
      }
      return manager;
    }

    /**
     * Specifies the pool size of the manager.
     *
     * @param poolSize The pool size.
     * @return This builder instance.
     */
    public Builder poolSize(final Integer poolSize) {

      if (poolSize != null) {
        AssertUtils.isTrue(
            poolSize >= 0 && poolSize <= MAX_POOL_SIZE,
            String.format("poolSize %s must be between %d and %d", poolSize, 1, MAX_POOL_SIZE));
      }
      this.poolSize = poolSize;
      return this;
    }
  }
}
