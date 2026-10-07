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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.office.AbstractOfficeWorkerPool;

/**
 * The properties shared by the office managers of the starter, which are all pools of office
 * workers: the local, external and remote properties records implement this interface.
 */
public interface JodConverterPoolProperties {

  /**
   * Gets the directory where temporary files will be created.
   *
   * @return The directory, or null for the system temporary directory.
   */
  @Nullable String workingDir();

  /**
   * Gets the maximum number of tasks waiting in the conversion queue.
   *
   * @return The capacity; 0 means no limit.
   */
  int taskQueueCapacity();

  /**
   * Gets the maximum living time of a task in the conversion queue.
   *
   * @return The timeout.
   */
  @NonNull Duration taskQueueTimeout();

  /**
   * Gets the maximum time allowed to process a task.
   *
   * @return The timeout.
   */
  @NonNull Duration taskExecutionTimeout();

  /**
   * Applies these properties to the given builder.
   *
   * @param builder The builder of an office manager.
   */
  default void applyTo(final AbstractOfficeWorkerPool.AbstractOfficeWorkerPoolBuilder<?> builder) {
    builder
        .workingDir(workingDir())
        .taskQueueCapacity(taskQueueCapacity())
        .taskQueueTimeout(taskQueueTimeout().toMillis())
        .taskExecutionTimeout(taskExecutionTimeout().toMillis());
  }
}
