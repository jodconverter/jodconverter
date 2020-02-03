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

package org.jodconverter.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Contains tests for the {@link JodConverterBean} class. */
public class SpringControllerTest {

  /* default */ @TempDir File testFolder;

  @Test
  public void bean_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues() {

    final JodConverterBean bean = new JodConverterBean();

    assertThat(bean)
        .extracting(
            "officeHome",
            "portNumbers",
            "workingDir",
            "templateProfileDir",
            "killExistingProcess",
            "processTimeout",
            "processRetryInterval",
            "taskExecutionTimeout",
            "maxTasksPerProcess",
            "taskQueueTimeout")
        .containsExactly(null, null, null, null, true, 120_000L, 250L, 120_000L, 200, 30_000L);
  }

  @Test
  public void bean_WithSpecifiedValues_ShouldInitializedOfficeManagerWithSpecifiedValues() {

    final JodConverterBean bean = new JodConverterBean();
    bean.setOfficeHome(new File(testFolder, "officeHome").getPath());
    bean.setPortNumbers("2005");
    bean.setWorkingDir(new File(testFolder, "workingDir").getPath());
    bean.setTemplateProfileDir(new File(testFolder, "templateProfileDir").getPath());
    bean.setKillExistingProcess(true);
    bean.setProcessTimeout(60_000L);
    bean.setProcessRetryInterval(1_000L);
    bean.setTaskExecutionTimeout(60_000L);
    bean.setMaxTasksPerProcess(20);
    bean.setTaskQueueTimeout(60_000L);

    assertThat(bean)
        .extracting(
            "officeHome",
            "portNumbers",
            "workingDir",
            "templateProfileDir",
            "killExistingProcess",
            "processTimeout",
            "processRetryInterval",
            "taskExecutionTimeout",
            "maxTasksPerProcess",
            "taskQueueTimeout")
        .containsExactly(
            new File(testFolder, "officeHome").getPath(),
            "2005",
            new File(testFolder, "workingDir").getPath(),
            new File(testFolder, "templateProfileDir").getPath(),
            true,
            60_000L,
            1_000L,
            60_000L,
            20,
            60_000L);
  }
}
