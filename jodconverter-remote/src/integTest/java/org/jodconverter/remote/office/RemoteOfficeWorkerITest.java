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

package org.jodconverter.remote.office;

import static org.assertj.core.api.Assertions.*;
import static org.jodconverter.remote.office.RemoteOfficeManager.DEFAULT_CONNECT_TIMEOUT;
import static org.jodconverter.remote.office.RemoteOfficeManager.DEFAULT_SOCKET_TIMEOUT;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.task.SimpleOfficeTask;

/** Contains tests for the {@link RemoteOfficeWorker} class. */
class RemoteOfficeWorkerITest {

  private static RemoteOfficeWorker newWorker(final String connectionUrl) {
    return new RemoteOfficeWorker(
        connectionUrl, null, DEFAULT_CONNECT_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
  }

  @Nested
  class Lifecycle {

    @Test
    void shouldAlwaysBeReadyAndHaveNothingToStartOrStop() {

      final var worker = newWorker("http://localhost/");

      assertThatCode(
              () -> {
                worker.start();
                worker.restart();
                worker.abort();
                worker.stop();
              })
          .doesNotThrowAnyException();
      assertThat(worker.isReady()).isTrue();
    }
  }

  @Nested
  class Execute {

    @Test
    void whenMalformedUrlExceptionCatch_ShouldThrowOfficeException() {

      final var worker = newWorker("localhost");

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> worker.execute(new SimpleOfficeTask()))
          .withCauseExactlyInstanceOf(MalformedURLException.class);
    }

    @Test
    void whenIOExceptionExceptionCatch_ShouldThrowOfficeException() {

      final var worker = newWorker("http://localhost/");

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> worker.execute(new SimpleOfficeTask(new IOException())))
          .withCauseExactlyInstanceOf(IOException.class);
    }
  }

  @Nested
  class BuildUrl {

    @Test
    void withAllValidUrlOptions_ShoulReturnProperUrlWithLoolExtension() {

      final var worker = newWorker("http://localhost/");

      String url =
          ReflectionTestUtils.invokeMethod(worker, "buildUrl", "http://localhost/lool/convert-to");
      assertThat(url).isEqualTo("http://localhost/lool/convert-to/");
      url =
          ReflectionTestUtils.invokeMethod(worker, "buildUrl", "http://localhost/lool/convert-to/");
      assertThat(url).isEqualTo("http://localhost/lool/convert-to/");
      url = ReflectionTestUtils.invokeMethod(worker, "buildUrl", "http://localhost");
      assertThat(url).isEqualTo("http://localhost/lool/convert-to/");
      url = ReflectionTestUtils.invokeMethod(worker, "buildUrl", "http://localhost/");
      assertThat(url).isEqualTo("http://localhost/lool/convert-to/");
    }
  }
}
