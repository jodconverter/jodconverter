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

package org.jodconverter.online.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.SimpleOfficeTask;

public class OnlineOfficeManagerPoolEntryITest {

  @Test
  public void execute_WhenMalformedUrlExceptionCatch_ShouldThrowOfficeException() throws Exception {

    final OnlineOfficeManagerPoolConfig config = new OnlineOfficeManagerPoolConfig(null);
    config.setWorkingDir(new File(System.getProperty("java.io.tmpdir")));

    final OnlineOfficeManagerPoolEntry officeManager =
        new OnlineOfficeManagerPoolEntry("localhost", null, config);
    try {
      officeManager.start();

      assertThat(officeManager.isRunning()).isTrue();
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> officeManager.execute(new SimpleOfficeTask()))
          .withCauseExactlyInstanceOf(MalformedURLException.class);
    } finally {

      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
    }
  }

  @Test
  public void execute_WhenIoExceptionExceptionCatch_ShouldThrowOfficeException() throws Exception {

    final OnlineOfficeManagerPoolConfig config = new OnlineOfficeManagerPoolConfig(null);
    config.setWorkingDir(new File(System.getProperty("java.io.tmpdir")));

    final OnlineOfficeManagerPoolEntry officeManager =
        new OnlineOfficeManagerPoolEntry("http://localhost/", null, config);
    try {
      officeManager.start();

      assertThat(officeManager.isRunning()).isTrue();
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> officeManager.execute(new SimpleOfficeTask(new IOException())))
          .withCauseExactlyInstanceOf(IOException.class);
    } finally {

      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
    }
  }

  @Test
  public void buildUrl_WithAllValidUrlOptions_ShoulReturnProperUrlWithLoolExtension()
      throws Exception {

    final OnlineOfficeManagerPoolEntry officeManager =
        new OnlineOfficeManagerPoolEntry(
            "localhost", null, new OnlineOfficeManagerPoolConfig(null));

    String url =
        Whitebox.invokeMethod(officeManager, "buildUrl", "http://localhost/lool/convert-to");
    assertThat(url).isEqualTo("http://localhost/lool/convert-to/");
    url = Whitebox.invokeMethod(officeManager, "buildUrl", "http://localhost/lool/convert-to/");
    assertThat(url).isEqualTo("http://localhost/lool/convert-to/");
    url = Whitebox.invokeMethod(officeManager, "buildUrl", "http://localhost");
    assertThat(url).isEqualTo("http://localhost/lool/convert-to/");
    url = Whitebox.invokeMethod(officeManager, "buildUrl", "http://localhost/");
    assertThat(url).isEqualTo("http://localhost/lool/convert-to/");
  }
}
