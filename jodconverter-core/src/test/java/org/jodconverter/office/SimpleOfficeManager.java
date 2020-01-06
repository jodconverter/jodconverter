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
import java.util.stream.IntStream;

import org.apache.commons.lang3.Validate;

/**
 * {@link OfficeManager} pool implementation that does not depend on an office installation to
 * process conversion taks.
 */
public final class SimpleOfficeManager extends AbstractOfficeManagerPool {

  private final int poolSize;

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

    this.poolSize = poolSize;
  }

  @Override
  protected SimpleOfficeManagerPoolEntry[] createPoolEntries() {

    return IntStream.range(0, poolSize)
        .mapToObj(
            i -> new SimpleOfficeManagerPoolEntry((SimpleOfficeManagerPoolEntryConfig) config))
        .toArray(SimpleOfficeManagerPoolEntry[]::new);
  }

  /**
   * A builder for constructing a {@link SimpleOfficeManager}.
   *
   * @see SimpleOfficeManager
   */
  public static final class Builder extends AbstractOfficeManagerPoolBuilder<Builder> {

    /** The default size of the pool. */
    public static final int DEFAULT_POOL_SIZE = 1;

    /** The maximum size of the pool. */
    public static final int MAX_POOL_SIZE = 1000;

    private int poolSize = DEFAULT_POOL_SIZE;

    // Private ctor so only SimpleOfficeManager can initialize an instance of this builder.
    private Builder() {
      super();
    }

    @Override
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
  }
}
