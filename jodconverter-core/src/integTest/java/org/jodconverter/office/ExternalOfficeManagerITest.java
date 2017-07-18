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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExternalOfficeManagerITest {

  // TODO test auto-reconnection

  /**
   * Test the auto-reconnection...
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void executeTask() throws Exception {
    final OfficeUrl officeUrl = new OfficeUrl(2002);
    final OfficeProcess officeProcess = new OfficeProcess(officeUrl);
    officeProcess.start();
    Thread.sleep(2000); // NOSONAR
    final Integer exitCode = officeProcess.getExitCode();
    if (exitCode != null && exitCode.equals(Integer.valueOf(81))) {
      officeProcess.start(true);
      Thread.sleep(2000); // NOSONAR
    }

    final ExternalOfficeManager manager = new ExternalOfficeManager(officeUrl, true);
    manager.start();

    final MockOfficeTask task = new MockOfficeTask();
    manager.execute(task);
    assertTrue(task.isCompleted());

    manager.stop();

    officeProcess.forciblyTerminate(1000, 5000);
  }
}
