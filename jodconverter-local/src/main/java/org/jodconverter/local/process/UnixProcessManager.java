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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The process manager of Linux, macOS, FreeBSD and the other Unix systems: it finds a running
 * process by the command line that the JVM reads for the processes of the current user, without
 * running any command.
 */
public class UnixProcessManager implements ProcessManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(UnixProcessManager.class);

  private static class DefaultHolder { // NOPMD - Disable utility class name rule violation
    /* default */ static final UnixProcessManager INSTANCE = new UnixProcessManager();
  }

  /**
   * Gets the default instance of this manager.
   *
   * @return The default instance.
   */
  public static @NonNull UnixProcessManager getDefault() {
    return DefaultHolder.INSTANCE;
  }

  @Override
  public @NonNull Optional<ProcessHandle> find(final @NonNull ProcessQuery query) {

    final var pattern = query.commandLinePattern();
    LOGGER.trace("Finding a process whose command line matches {}", pattern);
    return ProcessHandle.allProcesses()
        .filter(
            process ->
                process
                    .info()
                    .commandLine()
                    .filter(commandLine -> pattern.matcher(commandLine).find())
                    .isPresent())
        .findFirst();
  }
}
