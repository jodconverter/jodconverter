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

import java.io.IOException;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.util.AssertUtils;

/** Provides services required to manage a running process. */
public interface ProcessManager {

  long PID_NOT_FOUND = -2;
  long PID_UNKNOWN = -1;

  /**
   * Indicates whether the pid of the process can be found using a command line.
   *
   * @return {@code true} if the pid can be found using a command line, {@code false} otherwise.
   */
  default boolean canFindPid() {
    return true;
  }

  /**
   * Finds a PID of a running process that has the specified command line.
   *
   * @param query A query used to find the process with the pid we are looking for.
   * @return The pid if found, {@link #PID_NOT_FOUND} if not, or {@link #PID_UNKNOWN} if this
   *     implementation is unable to find out
   * @throws IOException If an IO error occurs.
   */
  default long findPid(@NonNull ProcessQuery query) throws IOException {
    return PID_UNKNOWN;
  }

  /**
   * Kills the specified process.
   *
   * @param process The process to kill.
   * @param pid The id of the process to kill.
   * @throws IOException If an IO error occurs.
   */
  default void kill(@Nullable Process process, long pid) throws IOException {
    AssertUtils.notNull(process, "process must not be null");
    process.destroy();
  }
}
