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

package org.jodconverter.cli.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

// Class taken from
// org.apache.tomcat.util.log

/**
 * Per Thread System.err and System.out log capture data.
 *
 * @author Glenn L. Nielsen
 */
@SuppressWarnings("PMD")
class CaptureLog {

  private final ByteArrayOutputStream baos;
  private final PrintStream ps;

  protected CaptureLog() {
    baos = new ByteArrayOutputStream();
    ps = new PrintStream(baos);
  }

  protected PrintStream getStream() {
    return ps;
  }

  protected void reset() {
    baos.reset();
  }

  protected String getCapture() {
    return baos.toString();
  }
}
