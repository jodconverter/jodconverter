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

package org.jodconverter.boot.autoconfigure;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.convert.DurationUnit;

import org.jodconverter.core.office.AbstractOfficeWorkerPool;

/**
 * The properties shared by the office managers of the starter, which are all pools of office
 * workers: the local, external and remote properties extend this class.
 */
public abstract class JodConverterPoolProperties {

  /**
   * Directory where temporary files will be created. If not set, it defaults to the system
   * temporary directory as specified by the java.io.tmpdir system property.
   */
  private String workingDir;

  /**
   * Maximum number of tasks waiting in the conversion queue. A task submitted while the queue is
   * full fails at once. 0 means no limit.
   */
  private int taskQueueCapacity = AbstractOfficeWorkerPool.DEFAULT_TASK_QUEUE_CAPACITY;

  /**
   * Maximum living time of a task in the conversion queue. The task will be removed from the queue
   * if the waiting time is longer than this timeout. A plain number is in milliseconds.
   */
  @DurationUnit(ChronoUnit.MILLIS)
  private Duration taskQueueTimeout =
      Duration.ofMillis(AbstractOfficeWorkerPool.DEFAULT_TASK_QUEUE_TIMEOUT);

  /**
   * Maximum time allowed to process a task. If the processing time of a task is longer than this
   * timeout, this task will be aborted and the next task is processed. A plain number is in
   * milliseconds.
   */
  @DurationUnit(ChronoUnit.MILLIS)
  private Duration taskExecutionTimeout =
      Duration.ofMillis(AbstractOfficeWorkerPool.DEFAULT_TASK_EXECUTION_TIMEOUT);

  /**
   * Applies these properties to the given builder.
   *
   * @param builder The builder of an office manager.
   */
  public void applyTo(final AbstractOfficeWorkerPool.AbstractOfficeWorkerPoolBuilder<?> builder) {
    builder
        .workingDir(workingDir)
        .taskQueueCapacity(taskQueueCapacity)
        .taskQueueTimeout(taskQueueTimeout.toMillis())
        .taskExecutionTimeout(taskExecutionTimeout.toMillis());
  }

  public @Nullable String getWorkingDir() {
    return workingDir;
  }

  public void setWorkingDir(final @Nullable String workingDir) {
    this.workingDir = workingDir;
  }

  public int getTaskQueueCapacity() {
    return taskQueueCapacity;
  }

  public void setTaskQueueCapacity(final int taskQueueCapacity) {
    this.taskQueueCapacity = taskQueueCapacity;
  }

  public @NonNull Duration getTaskQueueTimeout() {
    return taskQueueTimeout;
  }

  public void setTaskQueueTimeout(final @NonNull Duration taskQueueTimeout) {
    this.taskQueueTimeout = taskQueueTimeout;
  }

  public @NonNull Duration getTaskExecutionTimeout() {
    return taskExecutionTimeout;
  }

  public void setTaskExecutionTimeout(final @NonNull Duration taskExecutionTimeout) {
    this.taskExecutionTimeout = taskExecutionTimeout;
  }
}
