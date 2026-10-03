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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An automatic restart strategy that immediately executes restart actions. This is the default
 * behavior that maintains backward compatibility with previous versions.
 */
public final class AutomaticRestartStrategy implements RestartStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticRestartStrategy.class);

  @Override
  public void onRestartRequired(
      final @NonNull RestartReason reason,
      final @NonNull Runnable restartAction,
      final @Nullable AvailabilityCallback availabilityCallback) {

    LOGGER.debug("Automatic restart triggered for reason: {}", reason);

    restartAction.run();
  }
}
