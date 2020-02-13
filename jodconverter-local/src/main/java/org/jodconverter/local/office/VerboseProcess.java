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

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.local.process.PumpStreamHandler;
import org.jodconverter.local.process.StreamPumper;

/** Wrapper class for a process we want to redirect the output and error stream. */
class VerboseProcess {

  private static final Logger LOGGER = LoggerFactory.getLogger(VerboseProcess.class);

  private final Process process;
  private final PumpStreamHandler streamHandler;

  /**
   * Creates a new wrapper for the given process.
   *
   * @param process The process for which the wrapper is created.
   */
  /* default */ VerboseProcess(final Process process) {
    super();

    Objects.requireNonNull(process, "process must not be null");

    this.process = process;

    streamHandler =
        new PumpStreamHandler(
            new StreamPumper(process.getInputStream(), LOGGER::info),
            new StreamPumper(process.getErrorStream(), LOGGER::error));
    streamHandler.start();
  }

  /**
   * Gets the process for this wrapper.
   *
   * @return The process.
   */
  /* default */ Process getProcess() {
    return process;
  }

  /**
   * Gets the exit code for the process.
   *
   * @return The exit code of the process, or null if not terminated yet.
   */
  /* default */ Integer getExitCode() {

    try {
      final int exitValue = process.exitValue();
      streamHandler.stop();
      return exitValue;

    } catch (IllegalThreadStateException ex) {
      LOGGER.trace("The Office process has not yet terminated.");
      return null;
    }
  }
}
