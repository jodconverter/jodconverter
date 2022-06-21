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

package org.jodconverter.local.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_EXECUTION_TIMEOUT;
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_QUEUE_TIMEOUT;
import static org.jodconverter.local.office.LocalOfficeManager.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.process.ProcessManager;
import org.jodconverter.local.process.TestProcessManager;

/** Contains tests for the {@link LocalOfficeManager} class. */
class LocalOfficeManagerTest {

  @TempDir File testFolder;

  @BeforeEach
  void setUpOfficeHome() {
    System.setProperty("office.home", new File("src/test/resources/oohome").getPath());
  }

  @AfterEach
  void tearDown() {
    System.setProperty("office.home", "");
  }

  @Nested
  class Make {

    @Test
    void shouldInitializedManagerWithDefaultValues() {

      final OfficeManager manager = LocalOfficeManager.make();

      assertThat(manager).isInstanceOf(LocalOfficeManager.class);
      assertThat(manager)
          .extracting("tempDir")
          .satisfies(
              o ->
                  assertThat(o)
                      .asInstanceOf(InstanceOfAssertFactories.FILE)
                      .hasParent(OfficeUtils.getDefaultWorkingDir()));
      assertThat(manager)
          .hasFieldOrPropertyWithValue("taskQueueTimeout", DEFAULT_TASK_QUEUE_TIMEOUT);
      assertThat(manager)
          .extracting("entries")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .isInstanceOf(LocalOfficeManagerPoolEntry.class)
                      .extracting(
                          "taskExecutionTimeout",
                          "maxTasksPerProcess",
                          "officeProcessManager.officeUrl.connectString",
                          "officeProcessManager.officeHome",
                          "officeProcessManager.processManager.class.name",
                          "officeProcessManager.runAsArgs",
                          "officeProcessManager.templateProfileDir",
                          "officeProcessManager.processTimeout",
                          "officeProcessManager.processRetryInterval",
                          "officeProcessManager.afterStartProcessDelay",
                          "officeProcessManager.existingProcessAction",
                          "officeProcessManager.startFailFast",
                          "officeProcessManager.keepAliveOnShutdown",
                          "officeProcessManager.disableOpengl",
                          "officeProcessManager.connection.officeUrl.connectString")
                      .containsExactly(
                          DEFAULT_TASK_EXECUTION_TIMEOUT,
                          DEFAULT_MAX_TASKS_PER_PROCESS,
                          new OfficeUrl(2002).getConnectString(),
                          LocalOfficeUtils.getDefaultOfficeHome(),
                          LocalOfficeUtils.findBestProcessManager().getClass().getName(),
                          Collections.EMPTY_LIST,
                          null,
                          DEFAULT_PROCESS_TIMEOUT,
                          DEFAULT_PROCESS_RETRY_INTERVAL,
                          DEFAULT_AFTER_START_PROCESS_DELAY,
                          DEFAULT_EXISTING_PROCESS_ACTION,
                          DEFAULT_START_FAIL_FAST,
                          DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
                          DEFAULT_DISABLE_OPENGL,
                          new OfficeUrl(2002).getConnectString()));
    }
  }

  @Nested
  class Install {

    @Test
    void shouldSetInstalledOfficeManagerHolder() {

      // Ensure we do not replace the current installed manager
      final OfficeManager installedManager = InstalledOfficeManagerHolder.getInstance();
      try {
        final OfficeManager manager = LocalOfficeManager.install();
        assertThat(InstalledOfficeManagerHolder.getInstance()).isEqualTo(manager);
      } finally {
        InstalledOfficeManagerHolder.setInstance(installedManager);
      }
    }
  }

  @Nested
  class Build {

    @Test
    @SuppressWarnings("RedundantArrayCreation")
    void withNullValues_ShouldInitializedManagerWithDefaultValues() {

      final OfficeManager manager =
          LocalOfficeManager.builder()
              .workingDir((String) null)
              .workingDir((File) null)
              .taskExecutionTimeout(null)
              .taskQueueTimeout(null)
              .pipeNames((String[]) null)
              .pipeNames(new String[] {})
              .hostName(null)
              .portNumbers((int[]) null)
              .portNumbers(new int[] {})
              .officeHome((String) null)
              .officeHome((File) null)
              .processManager((String) null)
              .processManager((ProcessManager) null)
              .officeHome((File) null)
              .runAsArgs((String[]) null)
              .runAsArgs(new String[] {})
              .templateProfileDir((String) null)
              .templateProfileDir((File) null)
              .templateProfileDirOrDefault((String) null)
              .templateProfileDirOrDefault((File) null)
              .processTimeout(null)
              .processRetryInterval(null)
              .afterStartProcessDelay(null)
              .existingProcessAction(null)
              .startFailFast(null)
              .keepAliveOnShutdown(null)
              .disableOpengl(null)
              .maxTasksPerProcess(null)
              .build();

      assertThat(manager).isInstanceOf(LocalOfficeManager.class);
      assertThat(manager)
          .extracting("tempDir")
          .satisfies(
              o ->
                  assertThat(o)
                      .asInstanceOf(InstanceOfAssertFactories.FILE)
                      .hasParent(OfficeUtils.getDefaultWorkingDir()));
      assertThat(manager)
          .hasFieldOrPropertyWithValue("taskQueueTimeout", DEFAULT_TASK_QUEUE_TIMEOUT);
      assertThat(manager)
          .extracting("entries")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .isInstanceOf(LocalOfficeManagerPoolEntry.class)
                      .extracting(
                          "taskExecutionTimeout",
                          "maxTasksPerProcess",
                          "officeProcessManager.officeUrl.connectString",
                          "officeProcessManager.officeHome",
                          "officeProcessManager.processManager.class.name",
                          "officeProcessManager.runAsArgs",
                          "officeProcessManager.templateProfileDir",
                          "officeProcessManager.processTimeout",
                          "officeProcessManager.processRetryInterval",
                          "officeProcessManager.afterStartProcessDelay",
                          "officeProcessManager.existingProcessAction",
                          "officeProcessManager.startFailFast",
                          "officeProcessManager.keepAliveOnShutdown",
                          "officeProcessManager.disableOpengl",
                          "officeProcessManager.connection.officeUrl.connectString")
                      .containsExactly(
                          DEFAULT_TASK_EXECUTION_TIMEOUT,
                          DEFAULT_MAX_TASKS_PER_PROCESS,
                          new OfficeUrl(2002).getConnectString(),
                          LocalOfficeUtils.getDefaultOfficeHome(),
                          LocalOfficeUtils.findBestProcessManager().getClass().getName(),
                          Collections.EMPTY_LIST,
                          null,
                          DEFAULT_PROCESS_TIMEOUT,
                          DEFAULT_PROCESS_RETRY_INTERVAL,
                          DEFAULT_AFTER_START_PROCESS_DELAY,
                          DEFAULT_EXISTING_PROCESS_ACTION,
                          DEFAULT_START_FAIL_FAST,
                          DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
                          DEFAULT_DISABLE_OPENGL,
                          new OfficeUrl(2002).getConnectString()));
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void withCustomValues_ShouldInitializedManagerWithCustomValues() throws IOException {

      final File workingDir = new File(testFolder, "temp");
      workingDir.mkdirs();

      final File ooHome = new File(testFolder, "oohomecustom");
      final File program = new File(ooHome, "program");
      program.mkdirs();
      new File(program, "soffice.bin").createNewFile(); // EXECUTABLE_DEFAULT
      new File(program, "soffice").createNewFile(); // EXECUTABLE_MAC
      new File(program, "soffice.exe").createNewFile(); // EXECUTABLE_WINDOWS
      final File macos = new File(ooHome, "MacOS");
      macos.mkdirs();
      new File(macos, "soffice").createNewFile(); // EXECUTABLE_MAC_41
      program.mkdirs();

      final File templateProfileDir = new File(testFolder, "template");
      new File(templateProfileDir, "user").mkdirs();

      final OfficeManager manager =
          builder()
              .workingDir(workingDir.getPath())
              .taskExecutionTimeout(500L)
              .taskQueueTimeout(501L)
              .pipeNames("test")
              .hostName("localhost")
              .portNumbers(2003)
              .officeHome(ooHome.getPath())
              .officeHome((File) null)
              .processManager(TestProcessManager.class.getName())
              .officeHome(ooHome.getPath())
              .runAsArgs("sudo")
              .templateProfileDir(templateProfileDir.getPath())
              .templateProfileDirOrDefault(templateProfileDir.getPath())
              .processTimeout(502L)
              .processRetryInterval(503L)
              .afterStartProcessDelay(10L)
              .existingProcessAction(ExistingProcessAction.CONNECT)
              .startFailFast(true)
              .keepAliveOnShutdown(true)
              .disableOpengl(true)
              .maxTasksPerProcess(99)
              .build();

      assertThat(manager).isInstanceOf(LocalOfficeManager.class);
      assertThat(manager)
          .extracting("tempDir")
          .satisfies(
              o ->
                  assertThat(o).asInstanceOf(InstanceOfAssertFactories.FILE).hasParent(workingDir));
      assertThat(manager).hasFieldOrPropertyWithValue("taskQueueTimeout", 501L);
      assertThat(manager)
          .extracting("entries")
          .asList()
          .hasSize(2)
          .allSatisfy(
              o -> {
                assertThat(o)
                    .extracting("officeProcessManager.processManager")
                    .isInstanceOf(TestProcessManager.class);
                assertThat(o)
                    .isInstanceOf(LocalOfficeManagerPoolEntry.class)
                    .extracting(
                        "taskExecutionTimeout",
                        "maxTasksPerProcess",
                        "officeProcessManager.officeHome",
                        "officeProcessManager.processManager.class.name",
                        "officeProcessManager.runAsArgs",
                        "officeProcessManager.templateProfileDir",
                        "officeProcessManager.processTimeout",
                        "officeProcessManager.processRetryInterval",
                        "officeProcessManager.afterStartProcessDelay",
                        "officeProcessManager.existingProcessAction",
                        "officeProcessManager.startFailFast",
                        "officeProcessManager.keepAliveOnShutdown",
                        "officeProcessManager.disableOpengl")
                    .containsExactly(
                        500L,
                        99,
                        ooHome,
                        TestProcessManager.class.getName(),
                        Stream.of("sudo").collect(Collectors.toList()),
                        templateProfileDir,
                        502L,
                        503L,
                        10L,
                        ExistingProcessAction.CONNECT,
                        true,
                        true,
                        true);
              })
          .satisfies(
              o ->
                  assertThat(o.get(0))
                      .isInstanceOf(LocalOfficeManagerPoolEntry.class)
                      .extracting(
                          "officeProcessManager.officeUrl.connectString",
                          "officeProcessManager.connection.officeUrl.connectString")
                      .containsExactly(
                          new OfficeUrl("localhost", 2003).getConnectString(),
                          new OfficeUrl("localhost", 2003).getConnectString()))
          .satisfies(
              o ->
                  assertThat(o.get(1))
                      .isInstanceOf(LocalOfficeManagerPoolEntry.class)
                      .extracting(
                          "officeProcessManager.officeUrl.connectString",
                          "officeProcessManager.connection.officeUrl.connectString")
                      .containsExactly(
                          new OfficeUrl("test").getConnectString(),
                          new OfficeUrl("test").getConnectString()));
    }

    @Test
    void whenInvalidProcessManager_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> LocalOfficeManager.builder().processManager("jod.Foo").build());
    }

    @Test
    void whenInvalidProcessTimeout_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> LocalOfficeManager.builder().processTimeout(-1L).build());
    }

    @Test
    void whenInvalidProcessRetryInterval_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  LocalOfficeManager.builder()
                      .processRetryInterval(MIN_PROCESS_RETRY_INTERVAL - 1)
                      .build());
      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  LocalOfficeManager.builder()
                      .processRetryInterval(MAX_PROCESS_RETRY_INTERVAL + 1)
                      .build());
    }

    @Test
    void whenInvalidMaxTasksPerProcess_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> LocalOfficeManager.builder().maxTasksPerProcess(-1).build());
    }

    @Test
    void withInvalidTemplateProfileDir_ShouldUseDefaultTemplateProfileDir() {

      final File templateProfileDir = new File(testFolder, "template");

      final OfficeManager manager =
          LocalOfficeManager.builder()
              .templateProfileDirOrDefault(templateProfileDir.getPath())
              .build();

      assertThat(manager).isInstanceOf(LocalOfficeManager.class);
      assertThat(manager)
          .extracting("entries")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .isInstanceOf(LocalOfficeManagerPoolEntry.class)
                      .hasFieldOrPropertyWithValue(
                          "officeProcessManager.templateProfileDir", null));
    }
  }
}
