/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

package org.jodconverter.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class OnlineOfficeManagerPoolEntryITest {

  @Test
  public void execute_WhenMalformedUrlExceptionCatch_ShouldThrowOfficeException() throws Exception {

    final OnlineOfficeManagerPoolConfig config = new OnlineOfficeManagerPoolConfig(null);
    config.setWorkingDir(new File(System.getProperty("java.io.tmpdir")));

    final OnlineOfficeManagerPoolEntry officeManager =
        new OnlineOfficeManagerPoolEntry("localhost", config);
    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();

      try {
        officeManager.execute(new SimpleOfficeTask());
        fail("OfficeException should have been thrown");
      } catch (Exception ex) {
        assertThat(ex)
            .isExactlyInstanceOf(OfficeException.class)
            .hasCauseExactlyInstanceOf(MalformedURLException.class);
      }
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
        new OnlineOfficeManagerPoolEntry("http://localhost/", config);
    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();

      try {
        officeManager.execute(new SimpleOfficeTask(new IOException()));
        fail("OfficeException should have been thrown");
      } catch (Exception ex) {
        assertThat(ex)
            .isExactlyInstanceOf(OfficeException.class)
            .hasCauseExactlyInstanceOf(IOException.class);
      }
    } finally {

      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
    }
  }

  @Test
  public void buildUrl_WithAllValidUrlOptions_ShoulReturnProperUrlWithLoolExtension()
      throws Exception {

    final OnlineOfficeManagerPoolEntry officeManager =
        new OnlineOfficeManagerPoolEntry("localhost", new OnlineOfficeManagerPoolConfig(null));

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
