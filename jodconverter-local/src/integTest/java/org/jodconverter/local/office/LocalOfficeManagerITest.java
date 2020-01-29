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

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import org.jodconverter.core.office.AbstractOfficeManagerPool;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.SimpleOfficeTask;

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
    final OfficeProcessManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly
          .assertThat(config.getOfficeHome().getPath())
          .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
      softly
          .assertThat(config.getWorkingDir().getPath())
          .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
      softly
          .assertThat(config.getProcessManager())
          .isEqualTo(LocalOfficeUtils.findBestProcessManager());
      softly.assertThat(config.getRunAsArgs()).isNull();
      softly.assertThat(config.getTemplateProfileDir()).isNull();
      softly.assertThat(config.isKillExistingProcess()).isTrue();
      softly.assertThat(config.getProcessTimeout()).isEqualTo(120_000L);
      softly.assertThat(config.getProcessRetryInterval()).isEqualTo(250L);
      softly.assertThat(config.getMaxTasksPerProcess()).isEqualTo(200);
      softly.assertThat(config.isDisableOpengl()).isFalse();
      softly.assertThat(config.getTaskExecutionTimeout()).isEqualTo(120_000L);
      softly.assertThat(config.getTaskQueueTimeout()).isEqualTo(30_000L);
    }

    final OfficeUrl[] officeUrls = Whitebox.getInternalState(manager, "officeUrls");
    assertThat(officeUrls).hasSize(1);
    assertThat(officeUrls[0].getConnectionAndParametersAsString())
        .isEqualTo("socket,host=127.0.0.1,port=2002,tcpNoDelay=1");
  }

  @Test
  public void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues() {

    final OfficeManager manager =
        LocalOfficeManager.builder()
            .pipeNames("test")
            .portNumbers(2003)
            .officeHome(LocalOfficeUtils.getDefaultOfficeHome())
            .workingDir(System.getProperty("java.io.tmpdir"))
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

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly
          .assertThat(config.getOfficeHome().getPath())
          .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
      softly
          .assertThat(config.getWorkingDir().getPath())
          .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
      softly
          .assertThat(config.getTemplateProfileDir().getPath())
          .isEqualTo(new File("src/integTest/resources/templateProfileDir").getPath());
      softly
          .assertThat(config.getProcessManager())
          .isEqualTo(LocalOfficeUtils.findBestProcessManager());
      softly.assertThat(config.getRunAsArgs()).isEqualTo(new String[] {"sudo"});
      softly.assertThat(config.isKillExistingProcess()).isEqualTo(false);
      softly.assertThat(config.getProcessTimeout()).isEqualTo(5_000L);
      softly.assertThat(config.getProcessRetryInterval()).isEqualTo(1_000L);
      softly.assertThat(config.getMaxTasksPerProcess()).isEqualTo(10);
      softly.assertThat(config.isDisableOpengl()).isEqualTo(true);
      softly.assertThat(config.getTaskExecutionTimeout()).isEqualTo(20_000L);
      softly.assertThat(config.getTaskQueueTimeout()).isEqualTo(1_000L);
    }

    final OfficeUrl[] officeUrls = Whitebox.getInternalState(manager, "officeUrls");
    assertThat(officeUrls).hasSize(2);
    assertThat(officeUrls[0].getConnectionAndParametersAsString()).isEqualTo("pipe,name=test");
    assertThat(officeUrls[1].getConnectionAndParametersAsString())
        //    .isEqualTo("socket,host=localhost,port=2003");
        .isEqualTo("socket,host=127.0.0.1,port=2003,tcpNoDelay=1");
  }

  @Test
  public void
      build_WithInvalidTemplateProfileAndDefault_ShouldInitializedOfficeManagerWithDefaults() {

    final OfficeManager manager =
        LocalOfficeManager.builder().templateProfileDirOrDefault("src/foo/foo/foo/foo/foo").build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
    assertThat(config.getTemplateProfileDir()).isNull();
  }

  @Test
  public void build_WithValuesAsString_ShouldInitializedOfficeManagerWithCustomValues() {

    final OfficeManager manager =
        LocalOfficeManager.builder()
            .officeHome(LocalOfficeUtils.getDefaultOfficeHome().getPath())
            .workingDir(new File(System.getProperty("java.io.tmpdir")).getPath())
            .processManager(LocalOfficeUtils.findBestProcessManager().getClass().getName())
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getProcessManager().getClass().getName())
        .isEqualTo(LocalOfficeUtils.findBestProcessManager().getClass().getName());
  }

  @Test
  public void build_WithEmptyValuesAsString_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager =
        LocalOfficeManager.builder()
            .officeHome("   ")
            .workingDir("   ")
            .processManager("   ")
            .templateProfileDir("   ")
            .templateProfileDirOrDefault("   ")
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getProcessManager()).isEqualTo(LocalOfficeUtils.findBestProcessManager());
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
  public void build_WithNullPipeNames_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> LocalOfficeManager.builder().pipeNames((String[]) null).build());
  }

  @Test
  public void build_WithEmptyPipeNames_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> LocalOfficeManager.builder().pipeNames(new String[0]).build());
  }

  @Test
  public void build_WithNullPortNumbers_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> LocalOfficeManager.builder().portNumbers((int[]) null).build());
  }

  @Test
  public void build_WithEmptyPortNumbers_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> LocalOfficeManager.builder().portNumbers(new int[0]).build());
  }

  @Test
  public void build_WithNullRunAsArgs_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> LocalOfficeManager.builder().runAsArgs((String[]) null).build());
  }

  @Test
  public void build_WithEmptyRunAsArgs_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> LocalOfficeManager.builder().runAsArgs(new String[0]).build());
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
  public void execute_WithoutBeeingStarted_ThrowIllegalStateException() {

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

    final LocalOfficeManager manager = LocalOfficeManager.builder().taskQueueTimeout(1000L).build();
    try {
      manager.start();

      // Create threads that will both execute a task taking 2 sec to execute.
      final SleepyOfficeTaskRunner runnable1 = new SleepyOfficeTaskRunner(manager, 2000L);
      final SleepyOfficeTaskRunner runnable2 = new SleepyOfficeTaskRunner(manager, 2000L);
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
