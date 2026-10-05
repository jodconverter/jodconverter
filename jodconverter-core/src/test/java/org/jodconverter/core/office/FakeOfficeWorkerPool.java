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
import java.util.List;

/**
 * An {@link AbstractOfficeWorkerPool} of {@link FakeOfficeWorker}s for testing purposes, with short
 * restart delays and a short idle check interval.
 */
public final class FakeOfficeWorkerPool extends AbstractOfficeWorkerPool {

  /** The delays between the attempts to make a worker ready, in milliseconds. */
  public static final long[] RESTART_DELAYS = {0L, 20L, 40L};

  /** How often an idle worker checks that it is still ready, in milliseconds. */
  public static final long IDLE_CHECK_INTERVAL = 20L;

  private FakeOfficeWorkerPool(final Builder builder) {
    super(
        builder.workingDir,
        builder.taskQueueTimeout,
        builder.taskExecutionTimeout,
        builder.taskQueueCapacity,
        builder.startFailFast);

    if (builder.workers != null) {
      setWorkers(builder.workers);
    }
    setRestartDelays(RESTART_DELAYS);
    setIdleCheckInterval(IDLE_CHECK_INTERVAL);
  }

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /** A builder for constructing a {@link FakeOfficeWorkerPool}. */
  public static final class Builder extends AbstractOfficeWorkerPoolBuilder<Builder> {

    private List<FakeOfficeWorker> workers;
    private boolean startFailFast = true;

    private Builder() {
      super();
    }

    /**
     * Specifies the workers of the pool.
     *
     * @param workers The workers.
     * @return This builder instance.
     */
    public Builder workers(final FakeOfficeWorker... workers) {
      this.workers = List.of(workers);
      return this;
    }

    /**
     * Specifies whether the start of the pool waits for the workers and fails if one cannot start.
     *
     * @param startFailFast Whether the start fails fast.
     * @return This builder instance.
     */
    public Builder startFailFast(final boolean startFailFast) {
      this.startFailFast = startFailFast;
      return this;
    }

    /**
     * Gets whether the built manager must be installed.
     *
     * @return {@code true} if {@link #install()} was called.
     */
    public boolean isInstall() {
      return install;
    }

    /**
     * Gets the working directory of the built manager.
     *
     * @return The working directory.
     */
    public File getWorkingDir() {
      return workingDir;
    }

    @Override
    public FakeOfficeWorkerPool build() {
      return new FakeOfficeWorkerPool(this);
    }
  }
}
