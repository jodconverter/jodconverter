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

package org.jodconverter.local.process;

import org.checkerframework.checker.nullness.qual.NonNull;

/** Contains the required information used to query for a running process. */
public class ProcessQuery {

  private final String command;
  private final String argument;

  /**
   * Constructs a new instance with the given command and argument.
   *
   * @param command The process command.
   * @param argument The process argument.
   */
  public ProcessQuery(final @NonNull String command, final @NonNull String argument) {
    super();

    this.command = command;
    this.argument = argument;
  }

  /**
   * Gets the arguments of the process to query.
   *
   * @return The process argument.
   */
  public @NonNull String getArgument() {
    return argument;
  }

  /**
   * Gets the command of the process to query.
   *
   * @return The process command.
   */
  public @NonNull String getCommand() {
    return command;
  }

  @Override
  public @NonNull String toString() {
    return "ProcessQuery{" + "command='" + command + '\'' + ", argument='" + argument + '\'' + '}';
  }
}
