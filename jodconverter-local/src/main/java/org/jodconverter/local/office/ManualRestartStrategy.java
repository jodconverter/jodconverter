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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A manual restart strategy that collects restart requests and allows external code to trigger
 * restarts. This strategy is useful when you want to control when office processes are restarted,
 * for example, to batch restarts or to restart during maintenance windows.
 */
public final class ManualRestartStrategy implements RestartStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(ManualRestartStrategy.class);

  private final List<PendingRestart> pendingRestarts = new CopyOnWriteArrayList<>();
  private final List<RestartEventListener> listeners = new CopyOnWriteArrayList<>();

  /**
   * A class ot encapsulate pending restart requests, including the reason for the restart and the
   * action to execute when the restart is triggered.
   */
  private static class PendingRestart {
    private final RestartReason reason;
    private final Runnable restartAction;

    /* default */ PendingRestart(final RestartReason reason, final Runnable restartAction) {
      this.reason = reason;
      this.restartAction = restartAction;
    }
  }

  /** Listener interface for restart events. */
  public interface RestartEventListener {
    /**
     * Called when a restart is requested but not yet executed.
     *
     * @param reason The reason for the restart.
     */
    void onRestartRequested(RestartReason reason);

    /**
     * Called when a restart has been executed.
     *
     * @param reason The reason for the restart.
     * @param success {@code true} if the restart completed without throwing an exception, {@code
     *     false} otherwise.
     */
    void onRestartExecuted(RestartReason reason, boolean success);
  }

  @Override
  public void onRestartRequired(
      final @NonNull RestartReason reason,
      final @NonNull Runnable restartAction,
      final @Nullable AvailabilityCallback availabilityCallback) {

    LOGGER.info("Manual restart requested for reason: {}", reason);

    // Immediately mark the pool entry as unavailable if callback is provided
    // This prevents new tasks from being submitted while waiting for manual restart
    if (availabilityCallback != null) {
      LOGGER.debug("Marking pool entry as unavailable due to pending restart");
      availabilityCallback.markUnavailable();
    }

    final PendingRestart pending = new PendingRestart(reason, restartAction);
    pendingRestarts.add(pending);

    for (final RestartEventListener listener : listeners) {
      try {
        listener.onRestartRequested(reason);
      } catch (final Exception ex) {
        LOGGER.error("Error notifying restart listener", ex);
      }
    }
  }

  /**
   * Adds a listener to be notified of restart events.
   *
   * @param listener The listener to add.
   */
  public void addRestartEventListener(final @NonNull RestartEventListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes a previously added listener.
   *
   * @param listener The listener to remove.
   */
  public void removeRestartEventListener(final @NonNull RestartEventListener listener) {
    listeners.remove(listener);
  }

  /**
   * Gets the number of pending restart requests.
   *
   * @return The number of pending restarts.
   */
  public int getPendingRestartCount() {
    return pendingRestarts.size();
  }

  /**
   * Gets a list of pending restart reasons.
   *
   * @return A list of reasons for pending restarts.
   */
  public @NonNull List<RestartReason> getPendingRestartReasons() {
    final List<RestartReason> reasons = new ArrayList<>();
    for (final PendingRestart pending : pendingRestarts) {
      reasons.add(pending.reason);
    }
    return reasons;
  }

  /**
   * Executes the next pending restart request.
   *
   * @return {@code true} if a restart was executed, {@code false} if there were no pending
   *     restarts.
   */
  public boolean executeNextRestart() {
    if (pendingRestarts.isEmpty()) {
      return false;
    }

    final PendingRestart pending = pendingRestarts.remove(0);
    executeRestart(pending);
    return true;
  }

  /**
   * Executes all pending restart requests.
   *
   * @return The number of restarts executed.
   */
  public int executeAllPendingRestarts() {
    int count = 0;

    while (!pendingRestarts.isEmpty()) {
      final PendingRestart pending = pendingRestarts.remove(0);

      executeRestart(pending);

      count++;
    }
    return count;
  }

  /**
   * Clears all pending restart requests without executing them.
   *
   * @return The number of restart requests that were cleared.
   */
  public int clearPendingRestarts() {
    final int count = pendingRestarts.size();
    pendingRestarts.clear();
    LOGGER.info("Cleared {} pending restart requests", count);
    return count;
  }

  private void executeRestart(final PendingRestart pending) {
    LOGGER.info("Executing manual restart for reason: {}", pending.reason);
    boolean success = false;
    try {
      pending.restartAction.run();
      success = true;
    } catch (final Exception ex) {
      LOGGER.error("Error executing restart action", ex);
    } finally {
      for (final RestartEventListener listener : listeners) {
        try {
          listener.onRestartExecuted(pending.reason, success);
        } catch (final Exception ex) {
          LOGGER.error("Error notifying restart listener", ex);
        }
      }
    }
  }
}
