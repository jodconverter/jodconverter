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

import java.io.IOException;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Finds and kills office processes that were not started by JODConverter. The processes that
 * JODConverter starts itself are handled through their {@link ProcessHandle}; a process manager is
 * only asked to find a process that already runs with the connection string JODConverter wants to
 * use, and to kill a process, which it does by default through its handle.
 */
public interface ProcessManager {

  /**
   * Finds a running process whose command line contains the command and the argument of the given
   * query, in this order.
   *
   * @param query The command and the argument to look for.
   * @return The process, or empty if no running process matches the query, or if this manager is
   *     not able to list the running processes.
   * @throws IOException If the running processes cannot be listed.
   */
  @NonNull Optional<ProcessHandle> find(@NonNull ProcessQuery query) throws IOException;

  /**
   * Kills a process and its descendants. The default implementation forcibly destroys them through
   * their handles, which works for the processes of the current user on every platform; a manager
   * that runs the office processes as another user must override it.
   *
   * @param process The process to kill.
   * @throws IOException If the process cannot be killed.
   */
  default void kill(final @NonNull ProcessHandle process) throws IOException {
    process.descendants().forEach(ProcessHandle::destroyForcibly);
    process.destroyForcibly();
  }
}
