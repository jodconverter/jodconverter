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

package org.jodconverter.process;

import java.io.IOException;

/** Provides services required to manage a running process. */
public interface ProcessManager {

  long PID_NOT_FOUND = -2;
  long PID_UNKNOWN = -1;

  /**
   * Finds a PID of a running process that has the specified command line.
   *
   * @param query A query used to find the process with the pid we are looking for.
   * @return The pid if found, {@link #PID_NOT_FOUND} if not, or {@link #PID_UNKNOWN} if this
   *     implementation is unable to find out
   * @throws IOException If an IO error occurs.
   */
  long findPid(ProcessQuery query) throws IOException;

  /**
   * Kills the specified process.
   *
   * @param process The process to kill.
   * @param pid The id of the process to kill.
   * @throws IOException If an IO error occurs.
   */
  void kill(Process process, long pid) throws IOException;
}
