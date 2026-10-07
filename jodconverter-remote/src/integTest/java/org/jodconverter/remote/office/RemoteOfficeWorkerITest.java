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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.jodconverter.remote.office.RemoteOfficeManager.DEFAULT_CONNECT_TIMEOUT;
import static org.jodconverter.remote.office.RemoteOfficeManager.DEFAULT_SOCKET_TIMEOUT;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.http.HttpClient;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.task.SimpleOfficeTask;
import org.jodconverter.remote.ssl.SslConfig;

/** Contains tests for the {@link RemoteOfficeWorker} class. */
class RemoteOfficeWorkerITest {

  private static RemoteOfficeWorker newWorker(final String serviceUrl) {
    return new RemoteOfficeWorker(
        new RequestConfig(serviceUrl, DEFAULT_CONNECT_TIMEOUT, DEFAULT_SOCKET_TIMEOUT), null, null);
  }

  @Nested
  class Lifecycle {

    @Test
    void shouldAlwaysBeReadyAndHaveNothingToStop() {

      final var worker = newWorker("http://localhost/lool/convert-to/");
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

    @Test
    void start_ShouldBuildTheHttpClientOnce() throws OfficeException {

      final var worker = newWorker("http://localhost/lool/convert-to/");
      worker.start();
      final var client = new HttpClient[2];
      worker.execute(context -> client[0] = ((RemoteOfficeContext) context).getHttpClient());
      worker.restart();
      worker.execute(context -> client[1] = ((RemoteOfficeContext) context).getHttpClient());

      assertThat(client[0]).isNotNull().isSameAs(client[1]);
    }

    @Test
    void start_WithBadSslMaterial_ShouldThrowOfficeException() {

      final var sslConfig = new SslConfig();
      sslConfig.setProtocol("NoSuchProtocol");
      final var worker =
          new RemoteOfficeWorker(
              new RequestConfig("https://localhost/lool/convert-to/", 0L, 0L), sslConfig, null);

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(worker::start)
          .withMessage("Could not create the SSL context")
          .withCauseExactlyInstanceOf(java.security.NoSuchAlgorithmException.class);
    }
  }

  @Nested
  class Execute {

    @Test
    void whenIOExceptionCatch_ShouldThrowOfficeException() {

      final var worker = newWorker("http://localhost/lool/convert-to/");
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> worker.execute(new SimpleOfficeTask(new IOException())))
          .withCauseExactlyInstanceOf(IOException.class);
    }

    @Test
    void shouldGiveTheRequestConfigToTheTask() throws OfficeException {

      final var worker = newWorker("http://localhost/lool/convert-to/");
      final var config = new RequestConfig[1];
      worker.execute(context -> config[0] = ((RemoteOfficeContext) context).getRequestConfig());

      assertThat(config[0])
          .isEqualTo(
              new RequestConfig(
                  "http://localhost/lool/convert-to/",
                  DEFAULT_CONNECT_TIMEOUT,
                  DEFAULT_SOCKET_TIMEOUT));
    }
  }

  @Nested
  class ServiceUrl {

    @Test
    void withAnyFormOfTheServerUrl_ShouldReturnTheConvertToUrl() {

      assertThat(RemoteOfficeManager.toServiceUrl("http://localhost/lool/convert-to"))
          .isEqualTo("http://localhost/lool/convert-to/");
      assertThat(RemoteOfficeManager.toServiceUrl("http://localhost/lool/convert-to/"))
          .isEqualTo("http://localhost/lool/convert-to/");
      assertThat(RemoteOfficeManager.toServiceUrl("http://localhost/lool"))
          .isEqualTo("http://localhost/lool/convert-to/");
      assertThat(RemoteOfficeManager.toServiceUrl("http://localhost/cool/"))
          .isEqualTo("http://localhost/cool/convert-to/");
      assertThat(RemoteOfficeManager.toServiceUrl("https://localhost:9980/cool/convert-to"))
          .isEqualTo("https://localhost:9980/cool/convert-to/");
      assertThat(RemoteOfficeManager.toServiceUrl("http://localhost"))
          .isEqualTo("http://localhost/lool/convert-to/");
      assertThat(RemoteOfficeManager.toServiceUrl("http://localhost/"))
          .isEqualTo("http://localhost/lool/convert-to/");
    }

    @Test
    void withAnInvalidUrl_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> RemoteOfficeManager.builder().urlConnection("localhost").build())
          .withMessageContaining("is not a valid URL")
          .withCauseExactlyInstanceOf(MalformedURLException.class);
    }
  }
}
