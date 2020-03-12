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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.io.File;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.AbstractOfficeManagerPool;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.office.SimpleOfficeTask;
import org.jodconverter.local.process.ProcessManager;

/** Contains tests for the {@link LocalOfficeManager} class. */
public class LocalOfficeManagerITest {

  private static class SleepyOfficeTaskRunner implements Runnable {

    private final OfficeManager manager;
    private final long sleep;
    public OfficeException exception;

    /* default */ SleepyOfficeTaskRunner(final OfficeManager manager, final long sleep) {
      this.manager = manager;
      this.sleep = sleep;
    }

    @Override
    public void run() {
      try {
        manager.execute(new SimpleOfficeTask(sleep));
      } catch (OfficeException e) {
        exception = e;
      }
    }
  }

  @Test
  public void install_ShouldSetInstalledOfficeManagerHolder() {

    // Ensure we do not replace the current installed manager
    final OfficeManager installedManager = InstalledOfficeManagerHolder.getInstance();
    try {
      final OfficeManager manager = LocalOfficeManager.install();
      assertThat(InstalledOfficeManagerHolder.getInstance()).isEqualTo(manager);
    } finally {
      InstalledOfficeManagerHolder.setInstance(installedManager);
    }
  }

  @Test
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager = LocalOfficeManager.make();

    assertThat(manager).isInstanceOf(LocalOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(OfficeProcessManagerPoolEntry.class)
                    .extracting(
                        "taskExecutionTimeout",
                        "maxTasksPerProcess",
                        "officeProcessManager.processTimeout",
                        "officeProcessManager.processRetryInterval",
                        "officeProcessManager.disableOpengl",
                        "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                        "officeProcessManager.process.officeHome",
                        "officeProcessManager.process.processManager",
                        "officeProcessManager.process.runAsArgs",
                        "officeProcessManager.process.templateProfileDir",
                        "officeProcessManager.process.killExistingProcess",
                        "officeProcessManager.process.instanceProfileDir",
                        "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                    .containsExactly(
                        120_000L,
                        200,
                        120_000L,
                        250L,
                        false,
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                        LocalOfficeUtils.getDefaultOfficeHome(),
                        LocalOfficeUtils.findBestProcessManager(),
                        Collections.EMPTY_LIST,
                        null,
                        true,
                        new File(
                            OfficeUtils.getDefaultWorkingDir(),
                            ".jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1"),
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
  }

  @Test
  public void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues() {

    final OfficeManager manager =
        LocalOfficeManager.builder()
            .pipeNames("test")
            .portNumbers(2003)
            .officeHome(LocalOfficeUtils.getDefaultOfficeHome())
            .workingDir(OfficeUtils.getDefaultWorkingDir())
            .templateProfileDir("src/integTest/resources/templateProfileDir")
            .processManager(LocalOfficeUtils.findBestProcessManager())
            .runAsArgs("sudo")
            .killExistingProcess(false)
            .processTimeout(5_000L)
            .processRetryInterval(1_000L)
            .maxTasksPerProcess(10)
            .disableOpengl(true)
            .taskExecutionTimeout(20_000L)
            .taskQueueTimeout(1_000L)
            .build();

    assertThat(manager).isInstanceOf(LocalOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 1_000L);
    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(2)
        .allSatisfy(
            o ->
                assertThat(o)
                    .isInstanceOf(OfficeProcessManagerPoolEntry.class)
                    .extracting(
                        "taskExecutionTimeout",
                        "maxTasksPerProcess",
                        "officeProcessManager.processTimeout",
                        "officeProcessManager.processRetryInterval",
                        "officeProcessManager.disableOpengl",
                        "officeProcessManager.process.officeHome",
                        "officeProcessManager.process.processManager",
                        "officeProcessManager.process.runAsArgs",
                        "officeProcessManager.process.templateProfileDir",
                        "officeProcessManager.process.killExistingProcess")
                    .containsExactly(
                        20_000L,
                        10,
                        5_000L,
                        1_000L,
                        true,
                        LocalOfficeUtils.getDefaultOfficeHome(),
                        LocalOfficeUtils.findBestProcessManager(),
                        Collections.singletonList("sudo"),
                        new File("src/integTest/resources/templateProfileDir"),
                        false))
        .satisfies(
            o -> {
              assertThat(o.get(0))
                  .isInstanceOf(OfficeProcessManagerPoolEntry.class)
                  .extracting(
                      "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                      "officeProcessManager.process.instanceProfileDir",
                      "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                  .containsExactly(
                      "socket,host=127.0.0.1,port=2003,tcpNoDelay=1",
                      new File(
                          OfficeUtils.getDefaultWorkingDir(),
                          ".jodconverter_socket_host-127.0.0.1_port-2003_tcpNoDelay-1"),
                      "socket,host=127.0.0.1,port=2003,tcpNoDelay=1");
              assertThat(o.get(1))
                  .isInstanceOf(OfficeProcessManagerPoolEntry.class)
                  .extracting(
                      "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                      "officeProcessManager.process.instanceProfileDir",
                      "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                  .containsExactly(
                      "pipe,name=test",
                      new File(OfficeUtils.getDefaultWorkingDir(), ".jodconverter_pipe_name-test"),
                      "pipe,name=test");
            });
  }

  @Test
  public void
      build_WithInvalidTemplateProfileAndDefault_ShouldInitializedOfficeManagerWithDefaults() {

    final OfficeManager manager =
        LocalOfficeManager.builder().templateProfileDirOrDefault("src/foo/foo/foo/foo/foo").build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(OfficeProcessManagerPoolEntry.class)
                    .extracting(
                        "taskExecutionTimeout",
                        "maxTasksPerProcess",
                        "officeProcessManager.processTimeout",
                        "officeProcessManager.processRetryInterval",
                        "officeProcessManager.disableOpengl",
                        "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                        "officeProcessManager.process.officeHome",
                        "officeProcessManager.process.processManager",
                        "officeProcessManager.process.runAsArgs",
                        "officeProcessManager.process.templateProfileDir",
                        "officeProcessManager.process.killExistingProcess",
                        "officeProcessManager.process.instanceProfileDir",
                        "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                    .containsExactly(
                        120_000L,
                        200,
                        120_000L,
                        250L,
                        false,
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                        LocalOfficeUtils.getDefaultOfficeHome(),
                        LocalOfficeUtils.findBestProcessManager(),
                        Collections.EMPTY_LIST,
                        null,
                        true,
                        new File(
                            OfficeUtils.getDefaultWorkingDir(),
                            ".jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1"),
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
  }

  @Test
  public void build_WithValuesAsString_ShouldInitializedOfficeManagerWithCustomValues() {

    final OfficeManager manager =
        LocalOfficeManager.builder()
            .officeHome(LocalOfficeUtils.getDefaultOfficeHome().getPath())
            .workingDir(OfficeUtils.getDefaultWorkingDir().getPath())
            .processManager(LocalOfficeUtils.findBestProcessManager().getClass().getName())
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(OfficeProcessManagerPoolEntry.class)
                    .extracting(
                        "taskExecutionTimeout",
                        "maxTasksPerProcess",
                        "officeProcessManager.processTimeout",
                        "officeProcessManager.processRetryInterval",
                        "officeProcessManager.disableOpengl",
                        "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                        "officeProcessManager.process.officeHome",
                        "officeProcessManager.process.processManager.class.name",
                        "officeProcessManager.process.runAsArgs",
                        "officeProcessManager.process.templateProfileDir",
                        "officeProcessManager.process.killExistingProcess",
                        "officeProcessManager.process.instanceProfileDir",
                        "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                    .containsExactly(
                        120_000L,
                        200,
                        120_000L,
                        250L,
                        false,
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                        LocalOfficeUtils.getDefaultOfficeHome(),
                        LocalOfficeUtils.findBestProcessManager().getClass().getName(),
                        Collections.EMPTY_LIST,
                        null,
                        true,
                        new File(
                            OfficeUtils.getDefaultWorkingDir(),
                            ".jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1"),
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
  }

  @Test
  public void build_WithProcessManagerClassNotFound_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                LocalOfficeManager.builder()
                    .processManager("org.jodconverter.notfound.ClassName")
                    .build());
  }

  @Test
  public void build_WithNullPipeNames_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager = LocalOfficeManager.builder().pipeNames((String[]) null).build();

    assertThat(manager).isInstanceOf(LocalOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(OfficeProcessManagerPoolEntry.class)
                    .extracting(
                        "taskExecutionTimeout",
                        "maxTasksPerProcess",
                        "officeProcessManager.processTimeout",
                        "officeProcessManager.processRetryInterval",
                        "officeProcessManager.disableOpengl",
                        "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                        "officeProcessManager.process.officeHome",
                        "officeProcessManager.process.processManager",
                        "officeProcessManager.process.runAsArgs",
                        "officeProcessManager.process.templateProfileDir",
                        "officeProcessManager.process.killExistingProcess",
                        "officeProcessManager.process.instanceProfileDir",
                        "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                    .containsExactly(
                        120_000L,
                        200,
                        120_000L,
                        250L,
                        false,
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                        LocalOfficeUtils.getDefaultOfficeHome(),
                        LocalOfficeUtils.findBestProcessManager(),
                        Collections.EMPTY_LIST,
                        null,
                        true,
                        new File(
                            OfficeUtils.getDefaultWorkingDir(),
                            ".jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1"),
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
  }

  @Test
  public void build_WithEmptyPipeNames_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager = LocalOfficeManager.builder().pipeNames(new String[0]).build();

    assertThat(manager).isInstanceOf(LocalOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(OfficeProcessManagerPoolEntry.class)
                    .extracting(
                        "taskExecutionTimeout",
                        "maxTasksPerProcess",
                        "officeProcessManager.processTimeout",
                        "officeProcessManager.processRetryInterval",
                        "officeProcessManager.disableOpengl",
                        "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                        "officeProcessManager.process.officeHome",
                        "officeProcessManager.process.processManager",
                        "officeProcessManager.process.runAsArgs",
                        "officeProcessManager.process.templateProfileDir",
                        "officeProcessManager.process.killExistingProcess",
                        "officeProcessManager.process.instanceProfileDir",
                        "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                    .containsExactly(
                        120_000L,
                        200,
                        120_000L,
                        250L,
                        false,
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                        LocalOfficeUtils.getDefaultOfficeHome(),
                        LocalOfficeUtils.findBestProcessManager(),
                        Collections.EMPTY_LIST,
                        null,
                        true,
                        new File(
                            OfficeUtils.getDefaultWorkingDir(),
                            ".jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1"),
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
  }

  @Test
  public void build_WithNullPortNumbers_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager = LocalOfficeManager.builder().portNumbers((int[]) null).build();

    assertThat(manager).isInstanceOf(LocalOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(OfficeProcessManagerPoolEntry.class)
                    .extracting(
                        "taskExecutionTimeout",
                        "maxTasksPerProcess",
                        "officeProcessManager.processTimeout",
                        "officeProcessManager.processRetryInterval",
                        "officeProcessManager.disableOpengl",
                        "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                        "officeProcessManager.process.officeHome",
                        "officeProcessManager.process.processManager",
                        "officeProcessManager.process.runAsArgs",
                        "officeProcessManager.process.templateProfileDir",
                        "officeProcessManager.process.killExistingProcess",
                        "officeProcessManager.process.instanceProfileDir",
                        "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                    .containsExactly(
                        120_000L,
                        200,
                        120_000L,
                        250L,
                        false,
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                        LocalOfficeUtils.getDefaultOfficeHome(),
                        LocalOfficeUtils.findBestProcessManager(),
                        Collections.EMPTY_LIST,
                        null,
                        true,
                        new File(
                            OfficeUtils.getDefaultWorkingDir(),
                            ".jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1"),
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
  }

  @Test
  public void build_WithNullValues_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager =
        LocalOfficeManager.builder()
            .pipeNames((String[]) null)
            .portNumbers((int[]) null)
            .officeHome((File) null)
            .officeHome((String) null)
            .workingDir((File) null)
            .workingDir((String) null)
            .templateProfileDir((File) null)
            .templateProfileDir((String) null)
            .processManager((ProcessManager) null)
            .processManager((String) null)
            .runAsArgs((String[]) null)
            .killExistingProcess(null)
            .processTimeout(null)
            .processRetryInterval(null)
            .maxTasksPerProcess(null)
            .disableOpengl(null)
            .taskExecutionTimeout(null)
            .taskQueueTimeout(null)
            .build();

    assertThat(manager).isInstanceOf(LocalOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(OfficeProcessManagerPoolEntry.class)
                    .extracting(
                        "taskExecutionTimeout",
                        "maxTasksPerProcess",
                        "officeProcessManager.processTimeout",
                        "officeProcessManager.processRetryInterval",
                        "officeProcessManager.disableOpengl",
                        "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                        "officeProcessManager.process.officeHome",
                        "officeProcessManager.process.processManager",
                        "officeProcessManager.process.runAsArgs",
                        "officeProcessManager.process.templateProfileDir",
                        "officeProcessManager.process.killExistingProcess",
                        "officeProcessManager.process.instanceProfileDir",
                        "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                    .containsExactly(
                        120_000L,
                        200,
                        120_000L,
                        250L,
                        false,
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                        LocalOfficeUtils.getDefaultOfficeHome(),
                        LocalOfficeUtils.findBestProcessManager(),
                        Collections.EMPTY_LIST,
                        null,
                        true,
                        new File(
                            OfficeUtils.getDefaultWorkingDir(),
                            ".jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1"),
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
  }

  @Test
  public void build_WithEmptyPortNumbers_ThrowIllegalArgumentException() {

    final OfficeManager manager = LocalOfficeManager.builder().portNumbers(new int[0]).build();

    assertThat(manager).isInstanceOf(LocalOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(OfficeProcessManagerPoolEntry.class)
                    .extracting(
                        "taskExecutionTimeout",
                        "maxTasksPerProcess",
                        "officeProcessManager.processTimeout",
                        "officeProcessManager.processRetryInterval",
                        "officeProcessManager.disableOpengl",
                        "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                        "officeProcessManager.process.officeHome",
                        "officeProcessManager.process.processManager",
                        "officeProcessManager.process.runAsArgs",
                        "officeProcessManager.process.templateProfileDir",
                        "officeProcessManager.process.killExistingProcess",
                        "officeProcessManager.process.instanceProfileDir",
                        "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                    .containsExactly(
                        120_000L,
                        200,
                        120_000L,
                        250L,
                        false,
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                        LocalOfficeUtils.getDefaultOfficeHome(),
                        LocalOfficeUtils.findBestProcessManager(),
                        Collections.EMPTY_LIST,
                        null,
                        true,
                        new File(
                            OfficeUtils.getDefaultWorkingDir(),
                            ".jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1"),
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
  }

  @Test
  public void build_WithNullRunAsArgs_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager = LocalOfficeManager.builder().runAsArgs((String[]) null).build();

    assertThat(manager).isInstanceOf(LocalOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(OfficeProcessManagerPoolEntry.class)
                    .extracting(
                        "taskExecutionTimeout",
                        "maxTasksPerProcess",
                        "officeProcessManager.processTimeout",
                        "officeProcessManager.processRetryInterval",
                        "officeProcessManager.disableOpengl",
                        "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                        "officeProcessManager.process.officeHome",
                        "officeProcessManager.process.processManager",
                        "officeProcessManager.process.runAsArgs",
                        "officeProcessManager.process.templateProfileDir",
                        "officeProcessManager.process.killExistingProcess",
                        "officeProcessManager.process.instanceProfileDir",
                        "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                    .containsExactly(
                        120_000L,
                        200,
                        120_000L,
                        250L,
                        false,
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                        LocalOfficeUtils.getDefaultOfficeHome(),
                        LocalOfficeUtils.findBestProcessManager(),
                        Collections.EMPTY_LIST,
                        null,
                        true,
                        new File(
                            OfficeUtils.getDefaultWorkingDir(),
                            ".jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1"),
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
  }

  @Test
  public void build_WithEmptyRunAsArgs_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager = LocalOfficeManager.builder().runAsArgs(new String[0]).build();

    assertThat(manager).isInstanceOf(LocalOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(OfficeProcessManagerPoolEntry.class)
                    .extracting(
                        "taskExecutionTimeout",
                        "maxTasksPerProcess",
                        "officeProcessManager.processTimeout",
                        "officeProcessManager.processRetryInterval",
                        "officeProcessManager.disableOpengl",
                        "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                        "officeProcessManager.process.officeHome",
                        "officeProcessManager.process.processManager",
                        "officeProcessManager.process.runAsArgs",
                        "officeProcessManager.process.templateProfileDir",
                        "officeProcessManager.process.killExistingProcess",
                        "officeProcessManager.process.instanceProfileDir",
                        "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                    .containsExactly(
                        120_000L,
                        200,
                        120_000L,
                        250L,
                        false,
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                        LocalOfficeUtils.getDefaultOfficeHome(),
                        LocalOfficeUtils.findBestProcessManager(),
                        Collections.EMPTY_LIST,
                        null,
                        true,
                        new File(
                            OfficeUtils.getDefaultWorkingDir(),
                            ".jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1"),
                        "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
  }

  @Test
  public void start_StartTwice_ThrowIllegalStateException() throws OfficeException {

    final LocalOfficeManager manager = LocalOfficeManager.make();
    try {
      manager.start();
      assertThatIllegalStateException().isThrownBy(manager::start);
    } finally {
      manager.stop();
    }
  }

  @Test
  public void start_WhenTerminated_ThrowIllegalStateException() throws OfficeException {

    final LocalOfficeManager manager = LocalOfficeManager.make();
    manager.start();
    manager.stop();
    assertThatIllegalStateException().isThrownBy(manager::start);
  }

  @Test
  public void stop_WhenTerminated_SecondStopIgnored() throws OfficeException {

    final LocalOfficeManager manager = LocalOfficeManager.make();
    manager.start();
    manager.stop();
    assertThatCode(manager::stop).doesNotThrowAnyException();
  }

  @Test
  public void execute_WithoutBeingStarted_ThrowIllegalStateException() {

    assertThatIllegalStateException()
        .isThrownBy(() -> LocalOfficeManager.make().execute(new SimpleOfficeTask()));
  }

  @Test
  public void execute_WhenTerminated_ThrowIllegalStateException() {

    final LocalOfficeManager manager = LocalOfficeManager.make();
    assertThatCode(
            () -> {
              try {
                manager.start();
              } finally {
                manager.stop();
              }
            })
        .doesNotThrowAnyException();

    assertThatIllegalStateException().isThrownBy(() -> manager.execute(new SimpleOfficeTask()));
  }

  @Test
  public void execute_TaskQueueTimeout_ThrowOfficeException() throws Exception {

    final LocalOfficeManager manager =
        LocalOfficeManager.builder().taskQueueTimeout(1_000L).build();
    try {
      manager.start();

      // Create threads that will both execute a task taking more than a second to execute.
      final SleepyOfficeTaskRunner runnable1 = new SleepyOfficeTaskRunner(manager, 2_000L);
      final SleepyOfficeTaskRunner runnable2 = new SleepyOfficeTaskRunner(manager, 1_500L);
      final Thread thread1 = new Thread(runnable1);
      final Thread thread2 = new Thread(runnable2);

      // Start the threads.
      thread1.start();
      Thread.sleep(250L);
      thread2.start();

      // Wait for thread to complete
      thread1.join();
      thread2.join();

      // Here, the second runnable should contain the task queue timeout exception
      assertThat(runnable1.exception).isNull();
      assertThat(runnable2.exception)
          .isExactlyInstanceOf(OfficeException.class)
          .hasMessageContaining("No office manager available after 1000 millisec");

    } finally {
      manager.stop();
    }
  }
}
