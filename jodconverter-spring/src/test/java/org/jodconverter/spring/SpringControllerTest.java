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
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_EXECUTION_TIMEOUT;
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_QUEUE_TIMEOUT;
import static org.jodconverter.local.office.LocalOfficeManager.*;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.local.office.ExistingProcessAction;

/** Contains tests for the {@link JodConverterBean} class. */
public class SpringControllerTest {

  /* default */ @TempDir File testFolder;

  @Test
  public void withDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues() {

    final JodConverterBean bean = new JodConverterBean();

    assertThat(bean)
        .extracting(
            "workingDir",
            "taskExecutionTimeout",
            "taskQueueTimeout",
            "portNumbers",
            "officeHome",
            "processManagerClass",
            "templateProfileDir",
            "useDefaultOnInvalidTemplateProfileDir",
            "processTimeout",
            "processRetryInterval",
            "afterStartProcessDelay",
            "existingProcessAction",
            "startFailFast",
            "keepAliveOnShutdown",
            "disableOpengl",
            "maxTasksPerProcess")
        .containsExactly(
            null,
            DEFAULT_TASK_EXECUTION_TIMEOUT,
            DEFAULT_TASK_QUEUE_TIMEOUT,
            null,
            null,
            null,
            null,
            null,
            DEFAULT_PROCESS_TIMEOUT,
            DEFAULT_PROCESS_RETRY_INTERVAL,
            DEFAULT_AFTER_START_PROCESS_DELAY,
            DEFAULT_EXISTING_PROCESS_ACTION,
            DEFAULT_START_FAIL_FAST,
            DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
            DEFAULT_DISABLE_OPENGL,
            DEFAULT_MAX_TASKS_PER_PROCESS);
  }

  @Test
  public void withSpecifiedValues_ShouldInitializedOfficeManagerWithSpecifiedValues() {

    final JodConverterBean bean = new JodConverterBean();
    bean.setWorkingDir(new File(testFolder, "workingDir").getPath());
    bean.setTaskExecutionTimeout(500L);
    bean.setTaskQueueTimeout(501L);
    bean.setPortNumbers("2005");
    bean.setOfficeHome(new File(testFolder, "officeHome").getPath());
    bean.setProcessManager("org.jodconverter.Foo");
    bean.setTemplateProfileDir(new File(testFolder, "templateProfileDir").getPath());
    bean.setTemplateProfileDirOrDefault(
        new File(testFolder, "templateProfileDirOrDefault").getPath());
    bean.setProcessTimeout(503L);
    bean.setProcessRetryInterval(504L);
    bean.setAfterStartProcessDelay(10L);
    bean.setExistingProcessAction(ExistingProcessAction.CONNECT);
    bean.setStartFailFast(true);
    bean.setKeepAliveOnShutdown(true);
    bean.setDisableOpengl(true);
    bean.setMaxTasksPerProcess(99);

    assertThat(bean)
        .extracting(
            "workingDir",
            "taskExecutionTimeout",
            "taskQueueTimeout",
            "portNumbers",
            "officeHome",
            "processManagerClass",
            "templateProfileDir",
            "useDefaultOnInvalidTemplateProfileDir",
            "processTimeout",
            "processRetryInterval",
            "afterStartProcessDelay",
            "existingProcessAction",
            "startFailFast",
            "keepAliveOnShutdown",
            "disableOpengl",
            "maxTasksPerProcess")
        .containsExactly(
            new File(testFolder, "workingDir").getPath(),
            500L,
            501L,
            "2005",
            new File(testFolder, "officeHome").getPath(),
            "org.jodconverter.Foo",
            new File(testFolder, "templateProfileDirOrDefault").getPath(),
            true,
            503L,
            504L,
            10L,
            ExistingProcessAction.CONNECT,
            true,
            true,
            true,
            99);
  }
}
