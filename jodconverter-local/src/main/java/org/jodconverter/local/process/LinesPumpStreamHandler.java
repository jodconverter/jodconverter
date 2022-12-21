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

import java.io.InputStream;

import org.checkerframework.checker.nullness.qual.NonNull;

/** Copies standard output and error of sub-processes to list of lines. */
public class LinesPumpStreamHandler extends PumpStreamHandler {

  /**
   * Construct a new {@code LinesPumpStreamHandler}.
   *
   * @param output The stream for output.
   * @param error The stream for errors.
   */
  public LinesPumpStreamHandler(
      final @NonNull InputStream output, final @NonNull InputStream error) {
    super(new LinesStreamPumper(output), new LinesStreamPumper(error));
  }

  @Override
  public @NonNull LinesStreamPumper getOutputPumper() {
    return (LinesStreamPumper) super.getOutputPumper();
  }

  @Override
  public @NonNull LinesStreamPumper getErrorPumper() {
    return (LinesStreamPumper) super.getErrorPumper();
  }
}
