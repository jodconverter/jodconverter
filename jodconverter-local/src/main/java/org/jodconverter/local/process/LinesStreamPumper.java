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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Read all lines from an input stream. */
public class LinesStreamPumper extends StreamPumper {

  private static class LinesConsumer implements LineConsumer {

    private final List<String> lines = new ArrayList<>();

    @Override
    public void consume(final String line) {
      lines.add(line);
    }
  }

  /**
   * Creates a new pumper for the specified stream.
   *
   * @param stream The input stream to read from.
   */
  public LinesStreamPumper(final InputStream stream) {
    super(stream, new LinesConsumer());
  }

  /**
   * Gets the lines read from the stream by this pumper.
   *
   * @return The command output lines.
   */
  public List<String> getLines() {
    return ((LinesConsumer) getConsumer()).lines;
  }
}
