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

package org.jodconverter.local.process;

import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A process manager that never finds a running process: an office process that already runs with
 * the connection string JODConverter wants to use is not detected. It kills the processes
 * JODConverter started through their handles, like the other managers. Used on the platforms where
 * the running processes cannot be listed.
 */
public class PureJavaProcessManager implements ProcessManager {

  private static class DefaultHolder { // NOPMD - Disable utility class name rule violation
    /* default */ static final PureJavaProcessManager INSTANCE = new PureJavaProcessManager();
  }

  /**
   * Gets the default instance of this manager.
   *
   * @return The default instance.
   */
  public static @NonNull PureJavaProcessManager getDefault() {
    return DefaultHolder.INSTANCE;
  }

  @Override
  public @NonNull Optional<ProcessHandle> find(final @NonNull ProcessQuery query) {
    return Optional.empty();
  }
}
