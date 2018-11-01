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

package org.jodconverter.process;

import java.io.InputStream;

/** Copies standard output and error of subprocesses to list of lines. */
public class LinesPumpStreamHandler extends PumpStreamHandler {

  /**
   * Construct a new {@code LinesPumpStreamHandler}.
   *
   * @param output The stream for output.
   * @param error The stream for errors.
   */
  public LinesPumpStreamHandler(final InputStream output, final InputStream error) {
    super(new LinesStreamPumper(output), new LinesStreamPumper(error));
  }

  @Override
  public LinesStreamPumper getOutputPumper() {
    return (LinesStreamPumper) super.getOutputPumper();
  }

  @Override
  public LinesStreamPumper getErrorPumper() {
    return (LinesStreamPumper) super.getErrorPumper();
  }
}
