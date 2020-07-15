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

package org.jodconverter.local.office;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Exception that is thrown when an office process already exists for a specified connection string
 * and that we must try to connect to the running process.
 */
public class ExistingProcessConnectException extends RuntimeException {

  final String accept;
  final long pid;
  final ExistingProcessAction action;

  /**
   * Constructs a new exception with the specified argument.
   *
   * @param accept The accept string (--accept argument) for which the office process is running.
   * @param pid The id of the running process that was started with the specified accept argument.
   * @param action {@link ExistingProcessAction} that leads to this exception to be thrown.
   */
  public ExistingProcessConnectException(
      final @NonNull String accept, final long pid, final @NonNull ExistingProcessAction action) {
    super();

    this.accept = accept;
    this.pid = pid;
    this.action = action;
  }
}
