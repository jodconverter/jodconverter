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

package org.jodconverter.local.office;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A restart strategy determines how office process restarts are handled. Implementations can choose
 * to restart immediately, defer restarts for manual triggering, or implement custom restart logic.
 */
public interface RestartStrategy {

  /**
   * Callback interface to control the availability of the office manager pool entry during restart
   * operations.
   */
  @FunctionalInterface
  interface AvailabilityCallback {
    /**
     * Marks the pool entry as unavailable. This prevents new tasks from being submitted to a
     * process that is being restarted or needs to be restarted.
     */
    void markUnavailable();
  }

  /**
   * Called when an office process restart is required.
   *
   * @param reason The reason why the restart is required.
   * @param restartAction The action to execute to perform the restart. This runnable encapsulates
   *     the actual restart logic.
   * @param availabilityCallback Optional callback to control pool entry availability. If the
   *     restart is deferred (manual strategy), this callback should be invoked immediately to mark
   *     the pool entry as unavailable. For automatic restarts, this callback is typically not
   *     needed as the restart executes immediately.
   */
  void onRestartRequired(
      @NonNull final RestartReason reason,
      @NonNull final Runnable restartAction,
      @Nullable final AvailabilityCallback availabilityCallback);

  /**
   * Creates an automatic restart strategy that immediately executes restart actions. This is the
   * default behavior that maintains backward compatibility.
   *
   * @return A new automatic restart strategy instance.
   */
  static @NonNull RestartStrategy automatic() {
    return new AutomaticRestartStrategy();
  }

  /**
   * Creates a manual restart strategy that collects restart requests and allows external triggering
   * of restarts.
   *
   * @return A new manual restart strategy instance.
   */
  static @NonNull RestartStrategy manual() {
    return new ManualRestartStrategy();
  }
}
