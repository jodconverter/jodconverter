/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2019 Simon Braconnier and contributors
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

import java.io.IOException;
import java.io.PrintStream;
import java.util.EmptyStackException;
import java.util.Stack;

// Class taken from
// org.apache.tomcat.util.log

/**
 * This helper class may be used to do sophisticated redirection of System.out and System.err on a
 * per Thread basis.
 *
 * <p>A stack is implemented per Thread so that nested startCapture and stopCapture can be used.
 *
 * @author Remy Maucherat
 * @author Glenn L. Nielsen
 */
@SuppressWarnings("PMD")
public class SystemLogHandler extends PrintStream {

  // ----------------------------------------------------- Instance Variables

  /** Wrapped PrintStream. */
  private final PrintStream out;

  /** Thread &lt;-&gt; CaptureLog associations. */
  private static final ThreadLocal<Stack<CaptureLog>> logs = new ThreadLocal<>();

  /** Spare CaptureLog ready for reuse. */
  private static final Stack<CaptureLog> reuse = new Stack<>();

  // ----------------------------------------------------------- Constructors

  /**
   * Construct the handler to capture the output of the given steam.
   *
   * @param wrapped The stream to capture
   */
  public SystemLogHandler(final PrintStream wrapped) {
    super(wrapped);
    out = wrapped;
  }

  // --------------------------------------------------------- Public Methods

  /** Start capturing thread's output. */
  public static void startCapture() {
    CaptureLog log = null;
    if (!reuse.isEmpty()) {
      try {
        log = reuse.pop();
      } catch (EmptyStackException e) {
        log = new CaptureLog();
      }
    } else {
      log = new CaptureLog();
    }
    Stack<CaptureLog> stack = logs.get();
    if (stack == null) {
      stack = new Stack<>();
      logs.set(stack);
    }
    stack.push(log);
  }

  /**
   * Stop capturing thread's output.
   *
   * @return The captured data
   */
  public static String stopCapture() {
    final Stack<CaptureLog> stack = logs.get();
    if (stack == null || stack.isEmpty()) {
      return null;
    }
    final CaptureLog log = stack.pop();
    if (log == null) {
      return null;
    }
    final String capture = log.getCapture();
    log.reset();
    reuse.push(log);
    return capture;
  }

  // ------------------------------------------------------ Protected Methods

  /**
   * Find PrintStream to which the output must be written to.
   *
   * @return the print stream
   */
  protected PrintStream findStream() {
    final Stack<CaptureLog> stack = logs.get();
    if (stack != null && !stack.isEmpty()) {
      final CaptureLog log = stack.peek();
      if (log != null) {
        final PrintStream ps = log.getStream();
        if (ps != null) {
          return ps;
        }
      }
    }
    return out;
  }

  // ---------------------------------------------------- PrintStream Methods

  @Override
  public void flush() {
    findStream().flush();
  }

  @Override
  public void close() {
    findStream().close();
  }

  @Override
  public boolean checkError() {
    return findStream().checkError();
  }

  @Override
  protected void setError() {
    // findStream().setError();
  }

  @Override
  public void write(final int b) {
    findStream().write(b);
  }

  @Override
  public void write(final byte[] b) throws IOException {
    findStream().write(b);
  }

  @Override
  public void write(final byte[] buf, final int off, final int len) {
    findStream().write(buf, off, len);
  }

  @Override
  public void print(final boolean b) {
    findStream().print(b);
  }

  @Override
  public void print(final char c) {
    findStream().print(c);
  }

  @Override
  public void print(final int i) {
    findStream().print(i);
  }

  @Override
  public void print(final long l) {
    findStream().print(l);
  }

  @Override
  public void print(final float f) {
    findStream().print(f);
  }

  @Override
  public void print(final double d) {
    findStream().print(d);
  }

  @Override
  public void print(final char[] s) {
    findStream().print(s);
  }

  @Override
  public void print(final String s) {
    findStream().print(s);
  }

  @Override
  public void print(final Object obj) {
    findStream().print(obj);
  }

  @Override
  public void println() {
    findStream().println();
  }

  @Override
  public void println(final boolean x) {
    findStream().println(x);
  }

  @Override
  public void println(final char x) {
    findStream().println(x);
  }

  @Override
  public void println(final int x) {
    findStream().println(x);
  }

  @Override
  public void println(final long x) {
    findStream().println(x);
  }

  @Override
  public void println(final float x) {
    findStream().println(x);
  }

  @Override
  public void println(final double x) {
    findStream().println(x);
  }

  @Override
  public void println(final char[] x) {
    findStream().println(x);
  }

  @Override
  public void println(final String x) {
    findStream().println(x);
  }

  @Override
  public void println(final Object x) {
    findStream().println(x);
  }
}
