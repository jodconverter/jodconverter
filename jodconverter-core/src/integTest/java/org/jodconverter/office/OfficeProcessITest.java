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
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Test;

public class OfficeProcessITest {

  private static OfficeProcess startOfficeProcess(final int port, final OfficeProcessConfig config)
      throws Exception {

    final OfficeUrl officeUrl = new OfficeUrl(port);
    final OfficeProcess officeProcess = new OfficeProcess(officeUrl, config);
    officeProcess.start();
    Thread.sleep(2000); // NOSONAR
    final Integer exitCode = officeProcess.getExitCode();
    if (exitCode != null && exitCode.equals(Integer.valueOf(81))) {
      officeProcess.start(true);
      Thread.sleep(2000); // NOSONAR
    }
    return officeProcess;
  }

  @Test
  public void checkForExistingProcess_WithKillExistingProcessOn_ShouldKillExistingProcess()
      throws Exception {

    // Starts an office process on port 2002
    OfficeProcess officeProcess = startOfficeProcess(2002, new OfficeProcessConfig());

    try {
      final OfficeProcessConfig config = new OfficeProcessConfig();
      config.setKillExistingProcess(true);
      officeProcess = startOfficeProcess(2002, config);

      assertThat(officeProcess.isRunning()).isTrue();

    } finally {
      officeProcess.forciblyTerminate(1000, 5000);
    }
  }

  @Test
  public void checkForExistingProcess_WithKillExistingProcessOff_ShouldThrowsOfficeException()
      throws Exception {

    // Starts an office process on port 2002
    OfficeProcess officeProcess = startOfficeProcess(2002, new OfficeProcessConfig());

    try {
      final OfficeProcessConfig config = new OfficeProcessConfig();
      config.setKillExistingProcess(false);
      officeProcess = startOfficeProcess(2002, config);
      fail();

    } catch (Exception ex) {

      assertThat(ex)
          .isExactlyInstanceOf(OfficeException.class)
          .hasMessageContaining("A process with acceptString")
          .hasMessageContaining("is already running");

    } finally {
      officeProcess.forciblyTerminate(1000, 5000);
    }
  }

  @Test
  public void prepareInstanceProfileDir_WithCustomProfileDir_ShouldCopyProfileDirToWorkingDir()
      throws Exception {

    final OfficeProcessConfig config = new OfficeProcessConfig();
    config.setTemplateProfileDir(new File("src/integTest/resources/templateProfileDir"));

    OfficeProcess officeProcess = null;
    try {
      officeProcess = startOfficeProcess(2002, config);
      final File instanceProfileDir =
          (File) FieldUtils.readField(officeProcess, "instanceProfileDir", true);
      assertThat(new File(instanceProfileDir, "user/customFile")).isFile();
    } finally {
      if (officeProcess != null) {
        officeProcess.forciblyTerminate(1000, 5000);
      }
    }
  }
}
