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

import org.checkerframework.checker.nullness.qual.NonNull;

/** Copies standard output and error of sub-processes to given stream pumpers. */
public class PumpStreamHandler {

  private final StreamPumper outputPumper;
  private final StreamPumper errorPumper;

  /**
   * Construct a new {@code PumpStreamHandler}.
   *
   * @param outputPumper the output {@code StreamPumper}.
   * @param errorPumper the error {@code StreamPumper}.
   */
  public PumpStreamHandler(
      final @NonNull StreamPumper outputPumper, final @NonNull StreamPumper errorPumper) {
    this.outputPumper = outputPumper;
    this.errorPumper = errorPumper;
  }

  /**
   * Gets the output {@code StreamPumper}.
   *
   * @return The output pumper.
   */
  public @NonNull StreamPumper getOutputPumper() {
    return outputPumper;
  }

  /**
   * Gets the error {@code StreamPumper}.
   *
   * @return The error pumper.
   */
  public @NonNull StreamPumper getErrorPumper() {
    return errorPumper;
  }

  /** Start the pumpers. */
  public void start() {
    outputPumper.start();
    errorPumper.start();
  }

  /** Stop pumping the streams. */
  public void stop() {
    try {
      outputPumper.join();
    } catch (InterruptedException e) {
      // ignore
    }
    try {
      errorPumper.join();
    } catch (InterruptedException e) {
      // ignore
    }
  }
}
