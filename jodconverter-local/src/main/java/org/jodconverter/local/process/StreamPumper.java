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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;

/** Read all lines from an input stream. */
public class StreamPumper extends Thread {

  private final InputStream stream;
  private final LineConsumer consumer;

  /** Provides a function to consume a line read from a stream. */
  @FunctionalInterface
  public interface LineConsumer {

    /**
     * Consumes a line read from the input stream.
     *
     * @param line The line to consume.
     */
    void consume(@NonNull String line);
  }

  /**
   * Creates a new pumper for the specified stream.
   *
   * @param stream The input stream to read from.
   * @param consumer The consumer of lines read from the input stream.
   */
  public StreamPumper(final @NonNull InputStream stream, final @NonNull LineConsumer consumer) {
    super();

    Objects.requireNonNull(stream, "stream must not be null");
    Objects.requireNonNull(stream, "consumer must not be null");

    this.stream = stream;
    this.consumer = consumer;
    this.setDaemon(true);
  }

  /**
   * Gets the consumer of lines read from the input stream.
   *
   * @return The consumer.
   */
  public @NonNull LineConsumer getConsumer() {
    return consumer;
  }

  @Override
  public void run() {

    try (BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()))) {
      String line = bufferedReader.readLine();
      while (line != null) {
        consumer.consume(line);
        line = bufferedReader.readLine();
      }
    } catch (IOException ignored) {
      // ignored
    }
  }
}
