/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

package org.jodconverter.office;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class VerboseProcess {

  private static final Logger LOGGER = LoggerFactory.getLogger(VerboseProcess.class);

  private final Process process;
  private final Monitor errorMonitor;
  private final Monitor outputMonitor;

  @FunctionalInterface
  private interface LineLogger {
    void log(String line);
  }

  private static class Monitor extends Thread {

    private final InputStream input;
    private final LineLogger logger;

    Monitor(final InputStream input, final LineLogger logger) {
      super();

      this.input = input;
      this.logger = logger;
    }

    @Override
    public void run() {

      try (BufferedReader bufferedReader =
          new BufferedReader(Channels.newReader(Channels.newChannel(input), "UTF-8"))) {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          logger.log(line);
        }
      } catch (IOException ex) {
        LOGGER.error("Unable to read from command input stream.", ex);
      }
    }
  }

  VerboseProcess(final Process process) {
    super();

    this.process = process;

    outputMonitor = new Monitor(process.getInputStream(), (line) -> LOGGER.info(line));
    errorMonitor = new Monitor(process.getErrorStream(), (line) -> LOGGER.error(line));
    outputMonitor.start();
    errorMonitor.start();
  }

  Process getProcess() {
    return process;
  }

  Integer getExitCode() {

    try {
      final int exitValue = process.exitValue();
      try {
        outputMonitor.join();
        errorMonitor.join();
      } catch (InterruptedException ex) {
        // Log the interruption
        LOGGER.warn(
            "The current thread was interrupted while waiting for command execution output.", ex);
      }
      return exitValue;

    } catch (IllegalThreadStateException ex) {
      LOGGER.trace("The Office process has not yet terminated.");
      return null;
    }
  }
}
