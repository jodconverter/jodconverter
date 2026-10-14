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

package org.jodconverter.local.office;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Contains tests for the {@link VerboseProcess} class. */
class VerboseProcessTest {

  // What the process wrote, as the logger received it: "LEVEL message".
  private final List<String> logged = new CopyOnWriteArrayList<>();
  private final AbstractAppender appender =
      new AbstractAppender("VerboseProcessTest", null, null, true, Property.EMPTY_ARRAY) {
        @Override
        public void append(final LogEvent event) {
          logged.add(event.getLevel() + " " + event.getMessage().getFormattedMessage());
        }
      };

  private static Logger logger() {
    return (Logger) LogManager.getLogger(VerboseProcess.class);
  }

  @BeforeEach
  void addAppender() {
    appender.start();
    logger().addAppender(appender);
  }

  @AfterEach
  void removeAppender() {
    logger().removeAppender(appender);
    appender.stop();
  }

  // A process that has exited and has left the given outputs to read.
  private static Process process(final String output, final String errorOutput) {
    return new Process() {
      @Override
      public OutputStream getOutputStream() {
        return OutputStream.nullOutputStream();
      }

      @Override
      public InputStream getInputStream() {
        return new ByteArrayInputStream(output.getBytes(Charset.defaultCharset()));
      }

      @Override
      public InputStream getErrorStream() {
        return new ByteArrayInputStream(errorOutput.getBytes(Charset.defaultCharset()));
      }

      @Override
      public int waitFor() {
        return 0;
      }

      @Override
      public int exitValue() {
        return 0;
      }

      @Override
      public void destroy() {
        // Nothing to destroy.
      }

      @Override
      public long pid() {
        return 1L;
      }
    };
  }

  // The two outputs are read by threads of their own: waits until the given count of lines is
  // logged, then a little more, to see a line that should not have been logged.
  private void awaitLogged(final int count) throws InterruptedException {
    final long deadline = System.nanoTime() + 5_000_000_000L;
    while (logged.size() < count && System.nanoTime() < deadline) {
      Thread.sleep(10L);
    }
    Thread.sleep(100L);
  }

  @Test
  void whenProcessWritesOnItsOutput_ShouldLogTheLinesAsInformation() throws InterruptedException {

    new VerboseProcess(process("first line\nsecond line\n", ""));
    awaitLogged(2);

    assertThat(logged).containsExactly("INFO first line", "INFO second line");
  }

  @Test
  void whenProcessWritesOnItsErrorOutput_ShouldLogTheLinesAsWarningsNotAsErrors()
      throws InterruptedException {

    // What LibreOffice writes at the first start of a profile, which is not an error of
    // JODConverter nor of a conversion.
    new VerboseProcess(process("", "Entity: line 1: parser error : Document is empty\n\n^\n"));
    awaitLogged(2);

    assertThat(logged)
        .containsExactly("WARN Entity: line 1: parser error : Document is empty", "WARN ^");
    assertThat(logged).noneMatch(line -> line.startsWith(Level.ERROR.name()));
  }

  @Test
  void whenProcessWritesBlankLines_ShouldNotLogThem() throws InterruptedException {

    new VerboseProcess(process("\n   \nline\n\n", "\n"));
    awaitLogged(1);

    assertThat(logged).containsExactly("INFO line");
  }
}
