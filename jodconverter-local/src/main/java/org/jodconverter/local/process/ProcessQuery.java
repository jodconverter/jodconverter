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

import java.util.Objects;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Describes a process to find: its command (the name of its executable, such as {@code soffice})
 * and an argument of its command line (such as the {@code --accept} argument of an office process).
 *
 * @param command The command of the process to find.
 * @param argument The argument that follows the command on the command line of the process.
 */
public record ProcessQuery(@NonNull String command, @NonNull String argument) {

  /**
   * Creates a new query.
   *
   * @param command The command of the process to find.
   * @param argument The argument that follows the command on the command line of the process.
   */
  public ProcessQuery {
    Objects.requireNonNull(command, "command must not be null");
    Objects.requireNonNull(argument, "argument must not be null");
  }

  /**
   * Gets the pattern that the command line of a process must contain to match this query: the
   * command, then the argument, with anything in between.
   *
   * @return The pattern.
   */
  public @NonNull Pattern commandLinePattern() {
    return Pattern.compile(Pattern.quote(command) + ".*" + Pattern.quote(argument));
  }

  @Override
  public @NonNull String toString() {
    return "ProcessQuery{" + "command='" + command + '\'' + ", argument='" + argument + '\'' + '}';
  }
}
