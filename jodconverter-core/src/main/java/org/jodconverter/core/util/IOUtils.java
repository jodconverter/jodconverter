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

package org.jodconverter.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import org.checkerframework.checker.nullness.qual.NonNull;

/** Contains IO helper functions. */
public final class IOUtils {

  // buffer size used for reading and writing
  private static final int BUFFER_SIZE = 8192;

  /**
   * Reads all char from an input stream and writes them to a string.
   *
   * @param in The input stream to read from, must not be {@code null}.
   * @param encoding The encoding to use, must not be {@code null}.
   * @return The input stream content, as a string, never {@code null}.
   * @throws IOException If an IO error occurs.
   */
  public static String toString(@NonNull final InputStream in, @NonNull final Charset encoding)
      throws IOException {
    AssertUtils.notNull(in, "in must not be null");
    AssertUtils.notNull(encoding, "encoding must not be null");

    final StringBuilder builder = new StringBuilder();
    try (Reader source = new BufferedReader(new InputStreamReader(in, encoding))) {

      // Inspired from private java.nio.file.Files.copy(InputStream, OutputStream)
      // long nread = 0L;
      final char[] buf = new char[BUFFER_SIZE];
      int n;
      while ((n = source.read(buf)) > 0) { // NOPMD - Allow assignment here
        builder.append(buf, 0, n);
        // nread += n;
      }
      // LOGGER.debug("Total number of characters read: {}", nread);
    }

    return builder.toString();
  }

  // Taken from private java.nio.file.Files.copy(InputStream, OutputStream)
  /**
   * Reads all bytes from an input stream and writes them to an output stream.
   *
   * @param in The input stream to read from, must not be {@code null}.
   * @param out The output stream to write to, must not be {@code null}.
   * @return The number of bytes read or written.
   * @throws IOException If an IO error occurs.
   */
  public static long copy(@NonNull final InputStream in, @NonNull final OutputStream out)
      throws IOException {
    AssertUtils.notNull(in, "in must not be null");
    AssertUtils.notNull(out, "out must not be null");

    long nread = 0L;
    final byte[] buf = new byte[BUFFER_SIZE];
    int n;
    while ((n = in.read(buf)) > 0) { // NOPMD - Allow assignment here
      out.write(buf, 0, n);
      nread += n;
    }
    return nread;
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private IOUtils() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
