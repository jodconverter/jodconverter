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

import java.io.File;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Test;

public class LocalOfficeManagerITest {

  private static class SleepyOfficeTaskRunner implements Runnable {

    private final OfficeManager manager;
    private final long sleep;
    public OfficeException exception;

    SleepyOfficeTaskRunner(final OfficeManager manager, final long sleep) {
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
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues()
      throws Exception {

    final OfficeManager manager = LocalOfficeManager.make();

    assertThat(manager).isInstanceOf(LocalOfficeManager.class);
    final OfficeProcessManagerPoolConfig config =
        (OfficeProcessManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getProcessManager()).isEqualTo(LocalOfficeUtils.findBestProcessManager());
    assertThat(config.getRunAsArgs()).isNull();
    assertThat(config.getTemplateProfileDir()).isNull();
    assertThat(config.isKillExistingProcess()).isTrue();
    assertThat(config.getProcessTimeout()).isEqualTo(120000L);
    assertThat(config.getProcessRetryInterval()).isEqualTo(250L);
    assertThat(config.getMaxTasksPerProcess()).isEqualTo(200);
    assertThat(config.getTaskExecutionTimeout()).isEqualTo(120000L);
    assertThat(config.getTaskQueueTimeout()).isEqualTo(30000L);

    final OfficeUrl[] officeUrls = (OfficeUrl[]) FieldUtils.readField(manager, "officeUrls", true);
    assertThat(officeUrls).hasSize(1);
    assertThat(officeUrls[0].getConnectionAndParametersAsString())
        .isEqualTo("socket,host=127.0.0.1,port=2002,tcpNoDelay=1");
  }

  @Test
  public void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues()
      throws Exception {

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
            .processTimeout(5000)
            .processRetryInterval(1000)
            .maxTasksPerProcess(10)
            .taskExecutionTimeout(20000)
            .taskQueueTimeout(1000)
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config =
        (OfficeProcessManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getTemplateProfileDir().getPath())
        .isEqualTo(new File("src/integTest/resources/templateProfileDir").getPath());
    assertThat(config.getProcessManager()).isEqualTo(LocalOfficeUtils.findBestProcessManager());
    assertThat(config.getRunAsArgs()).isEqualTo(new String[] {"sudo"});
    assertThat(config.isKillExistingProcess()).isEqualTo(false);
    assertThat(config.getProcessTimeout()).isEqualTo(5000L);
    assertThat(config.getProcessRetryInterval()).isEqualTo(1000L);
    assertThat(config.getMaxTasksPerProcess()).isEqualTo(10);
    assertThat(config.getTaskExecutionTimeout()).isEqualTo(20000L);
    assertThat(config.getTaskQueueTimeout()).isEqualTo(1000L);

    final OfficeUrl[] officeUrls = (OfficeUrl[]) FieldUtils.readField(manager, "officeUrls", true);
    assertThat(officeUrls).hasSize(2);
    assertThat(officeUrls[0].getConnectionAndParametersAsString()).isEqualTo("pipe,name=test");
    assertThat(officeUrls[1].getConnectionAndParametersAsString())
        .isEqualTo("socket,host=127.0.0.1,port=2003,tcpNoDelay=1");
  }

  @Test
  public void build_WithValuesAsString_ShouldInitializedOfficeManagerWithCustomValues()
      throws Exception {

    final OfficeManager manager =
        LocalOfficeManager.builder()
            .officeHome(LocalOfficeUtils.getDefaultOfficeHome().getPath())
            .workingDir(new File(System.getProperty("java.io.tmpdir")).getPath())
            .processManager(LocalOfficeUtils.findBestProcessManager().getClass().getName())
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config =
        (OfficeProcessManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getProcessManager().getClass().getName())
        .isEqualTo(LocalOfficeUtils.findBestProcessManager().getClass().getName());
  }

  @Test
  public void build_WithEmptyValuesAsString_ShouldInitializedOfficeManagerWithDefaultValues()
      throws Exception {

    final OfficeManager manager =
        LocalOfficeManager.builder()
            .officeHome("   ")
            .workingDir("   ")
            .processManager("   ")
            .templateProfileDir("   ")
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config =
        (OfficeProcessManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getProcessManager()).isEqualTo(LocalOfficeUtils.findBestProcessManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithProcessManagerClassNotFound_ThrowIllegalArgumentException()
      throws Exception {

    LocalOfficeManager.builder().processManager("org.jodconverter.notfound.ClassName").build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithNullPipeNames_ThrowIllegalArgumentException() throws Exception {

    LocalOfficeManager.builder().pipeNames((String[]) null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithEmptyPipeNames_ThrowIllegalArgumentException() throws Exception {

    LocalOfficeManager.builder().pipeNames(new String[0]).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithNullPortNumbers_ThrowIllegalArgumentException() throws Exception {

    LocalOfficeManager.builder().portNumbers((int[]) null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithEmptyPortNumbers_ThrowIllegalArgumentException() throws Exception {

    LocalOfficeManager.builder().portNumbers(new int[0]).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithNullRunAsArgs_ThrowIllegalArgumentException() throws Exception {

    LocalOfficeManager.builder().runAsArgs((String[]) null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithEmptyRunAsArgs_ThrowIllegalArgumentException() throws Exception {

    LocalOfficeManager.builder().runAsArgs(new String[0]).build();
  }

  @Test(expected = IllegalStateException.class)
  public void start_StartTwice_ThrowIllegalStateException() throws Exception {

    final LocalOfficeManager manager = LocalOfficeManager.make();
    try {
      manager.start();
      manager.start();
    } finally {
      manager.stop();
    }
  }

  @Test(expected = IllegalStateException.class)
  public void start_WhenTerminated_ThrowIllegalStateException() throws Exception {

    final LocalOfficeManager manager = LocalOfficeManager.make();
    manager.start();
    manager.stop();
    manager.start();
  }

  @Test
  public void stop_WhenTerminated_SecondStopIgnored() throws Exception {

    final LocalOfficeManager manager = LocalOfficeManager.make();
    manager.start();
    manager.stop();
    manager.stop();
  }

  @Test(expected = IllegalStateException.class)
  public void execute_WithoutBeeingStared_ThrowIllegalStateException() throws Exception {

    LocalOfficeManager.make().execute(new SimpleOfficeTask());
  }

  @Test(expected = IllegalStateException.class)
  public void execute_WhenTerminated_ThrowIllegalStateException() throws Exception {

    final LocalOfficeManager manager = LocalOfficeManager.make();
    try {
      manager.start();
    } finally {
      manager.stop();
    }

    manager.execute(new SimpleOfficeTask());
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
      Thread.sleep(250L); // NOSONAR
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
