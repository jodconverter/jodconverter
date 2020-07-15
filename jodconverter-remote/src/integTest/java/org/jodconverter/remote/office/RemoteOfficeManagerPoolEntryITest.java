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

package org.jodconverter.remote.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_EXECUTION_TIMEOUT;
import static org.jodconverter.remote.office.RemoteOfficeManager.DEFAULT_CONNECT_TIMEOUT;
import static org.jodconverter.remote.office.RemoteOfficeManager.DEFAULT_SOCKET_TIMEOUT;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.task.SimpleOfficeTask;

/** Contains tests for the {@link RemoteOfficeManagerPoolEntry} class. */
class RemoteOfficeManagerPoolEntryITest {

  @Nested
  class DoExecute {

    @Test
    void whenMalformedUrlExceptionCatch_ShouldThrowOfficeException() throws Exception {

      final RemoteOfficeManagerPoolEntry manager =
          new RemoteOfficeManagerPoolEntry(
              "localhost",
              null,
              DEFAULT_CONNECT_TIMEOUT,
              DEFAULT_SOCKET_TIMEOUT,
              DEFAULT_TASK_EXECUTION_TIMEOUT);
      try {
        manager.start();

        assertThat(manager.isRunning()).isTrue();
        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(() -> manager.execute(new SimpleOfficeTask()))
            .withCauseExactlyInstanceOf(MalformedURLException.class);
      } finally {

        manager.stop();
        assertThat(manager.isRunning()).isFalse();
      }
    }

    @Test
    void whenIOExceptionExceptionCatch_ShouldThrowOfficeException() throws Exception {

      final RemoteOfficeManagerPoolEntry manager =
          new RemoteOfficeManagerPoolEntry(
              "http://localhost/",
              null,
              DEFAULT_CONNECT_TIMEOUT,
              DEFAULT_SOCKET_TIMEOUT,
              DEFAULT_TASK_EXECUTION_TIMEOUT);
      try {
        manager.start();

        assertThat(manager.isRunning()).isTrue();
        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(() -> manager.execute(new SimpleOfficeTask(new IOException())))
            .withCauseExactlyInstanceOf(IOException.class);
      } finally {

        manager.stop();
        assertThat(manager.isRunning()).isFalse();
      }
    }
  }

  @Nested
  class BuildUrl {

    @Test
    void withAllValidUrlOptions_ShoulReturnProperUrlWithLoolExtension() throws Exception {

      final RemoteOfficeManagerPoolEntry manager =
          new RemoteOfficeManagerPoolEntry(
              "http://localhost/",
              null,
              DEFAULT_CONNECT_TIMEOUT,
              DEFAULT_SOCKET_TIMEOUT,
              DEFAULT_TASK_EXECUTION_TIMEOUT);

      String url = Whitebox.invokeMethod(manager, "buildUrl", "http://localhost/lool/convert-to");
      assertThat(url).isEqualTo("http://localhost/lool/convert-to/");
      url = Whitebox.invokeMethod(manager, "buildUrl", "http://localhost/lool/convert-to/");
      assertThat(url).isEqualTo("http://localhost/lool/convert-to/");
      url = Whitebox.invokeMethod(manager, "buildUrl", "http://localhost");
      assertThat(url).isEqualTo("http://localhost/lool/convert-to/");
      url = Whitebox.invokeMethod(manager, "buildUrl", "http://localhost/");
      assertThat(url).isEqualTo("http://localhost/lool/convert-to/");
    }
  }
}
