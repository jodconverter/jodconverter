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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExternalOfficeManagerITest {

  private static OfficeProcess officeProcess;

  /**
   * Starts an office process just once.
   *
   * @throws Exception If an error occurs.
   */
  @BeforeClass
  public static void setUpClass() throws Exception {

    final OfficeUrl officeUrl = new OfficeUrl(2002);
    officeProcess = new OfficeProcess(officeUrl);
    officeProcess.start();
    Thread.sleep(2000); // NOSONAR
    final Integer exitCode = officeProcess.getExitCode();
    if (exitCode != null && exitCode.equals(Integer.valueOf(81))) {
      officeProcess.start(true);
      Thread.sleep(2000); // NOSONAR
    }
  }

  /**
   * Stops the office process once the tests are all done.
   *
   * @throws Exception If an error occurs.
   */
  @AfterClass
  public static void tearDownClass() throws Exception {

    officeProcess.forciblyTerminate(1000, 5000);
    officeProcess.deleteProfileDir();
  }

  // TODO test auto-reconnection

  /**
   * Test connection with connect on start on.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void execute_WithConnectOnStart_TaskExecutedSuccessfully() throws Exception {

    final OfficeManager manager =
        new ExternalOfficeManagerBuilder()
            .setConnectionProtocol(OfficeConnectionProtocol.SOCKET)
            .setPortNumber(2002)
            .setConnectOnStart(true)
            .build();
    manager.start();
    try {

      final MockOfficeTask task = new MockOfficeTask();
      manager.execute(task);
      assertThat(task.isCompleted()).isTrue();

    } finally {
      LocalOfficeUtils.stopQuietly(manager);
    }
  }

  /**
   * Test connection with connect on start off.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void execute_WithoutConnectOnStart_TaskExecutedSuccessfully() throws Exception {

    final OfficeManager manager =
        new ExternalOfficeManagerBuilder()
            .setConnectionProtocol(OfficeConnectionProtocol.SOCKET)
            .setPortNumber(2002)
            .setConnectOnStart(false)
            .build();
    manager.start();
    try {

      final MockOfficeTask task = new MockOfficeTask();
      manager.execute(task);
      assertThat(task.isCompleted()).isTrue();

    } finally {
      LocalOfficeUtils.stopQuietly(manager);
    }
  }

  /**
   * Test connection without an office process.
   *
   * @throws Exception if an error occurs.
   */
  @Test(expected = OfficeException.class)
  public void connect_WithoutOfficeProcess_ThrowOfficeException() throws Exception {

    final OfficeManager manager =
        new ExternalOfficeManagerBuilder()
            .setConnectionProtocol(OfficeConnectionProtocol.SOCKET)
            .setPortNumber(2003)
            .setConnectOnStart(true)
            .setConnectTimeout(2000L)
            .build();
    manager.start();
  }
}
