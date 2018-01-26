/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

public class SimpleOfficeManagerTest {

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
      final OfficeManager manager = SimpleOfficeManager.install();
      assertThat(InstalledOfficeManagerHolder.getInstance()).isEqualTo(manager);
    } finally {
      InstalledOfficeManagerHolder.setInstance(installedManager);
    }
  }

  @Test
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues()
      throws Exception {

    final OfficeManager manager = SimpleOfficeManager.make();

    assertThat(manager).isInstanceOf(SimpleOfficeManager.class);
    final SimpleOfficeManagerPoolConfig config =
        (SimpleOfficeManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getTaskExecutionTimeout()).isEqualTo(120000L);
    assertThat(config.getTaskQueueTimeout()).isEqualTo(30000L);

    manager.start();
    try {
      final OfficeManager[] poolEntries =
          (OfficeManager[]) FieldUtils.readField(manager, "entries", true);
      assertThat(poolEntries).hasSize(1);
      assertThat(poolEntries[0]).isInstanceOf(SimpleOfficeManagerPoolEntry.class);
    } finally {
      manager.stop();
    }
  }

  @Test
  public void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues()
      throws Exception {

    final OfficeManager manager =
        SimpleOfficeManager.builder()
            .workingDir(System.getProperty("java.io.tmpdir"))
            .taskExecutionTimeout(20000)
            .taskQueueTimeout(1000)
            .poolSize(2)
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final SimpleOfficeManagerPoolConfig config =
        (SimpleOfficeManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getTaskExecutionTimeout()).isEqualTo(20000L);
    assertThat(config.getTaskQueueTimeout()).isEqualTo(1000L);

    manager.start();
    try {
      final OfficeManager[] poolEntries =
          (OfficeManager[]) FieldUtils.readField(manager, "entries", true);
      assertThat(poolEntries).hasSize(2);
      assertThat(poolEntries[0]).isInstanceOf(SimpleOfficeManagerPoolEntry.class);
    } finally {
      manager.stop();
    }
  }

  @Test
  public void build_WithValuesAsString_ShouldInitializedOfficeManagerWithCustomValues()
      throws Exception {

    final OfficeManager manager =
        SimpleOfficeManager.builder()
            .workingDir(new File(System.getProperty("java.io.tmpdir")).getPath())
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final SimpleOfficeManagerPoolConfig config =
        (SimpleOfficeManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
  }

  @Test
  public void build_WithEmptyValuesAsString_ShouldInitializedOfficeManagerWithDefaultValues()
      throws Exception {

    final OfficeManager manager = SimpleOfficeManager.builder().workingDir("   ").build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final SimpleOfficeManagerPoolConfig config =
        (SimpleOfficeManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
  }

  @Test(expected = IllegalStateException.class)
  public void start_StartTwice_ThrowIllegalStateException() throws Exception {

    final SimpleOfficeManager manager = SimpleOfficeManager.make();
    try {
      manager.start();
      manager.start();
    } finally {
      manager.stop();
    }
  }

  @Test(expected = IllegalStateException.class)
  public void start_WhenTerminated_ThrowIllegalStateException() throws Exception {

    final SimpleOfficeManager manager = SimpleOfficeManager.make();
    manager.start();
    manager.stop();
    manager.start();
  }

  @Test
  public void stop_WhenTerminated_SecondStopIgnored() throws Exception {

    final SimpleOfficeManager manager = SimpleOfficeManager.make();
    manager.start();
    manager.stop();
    manager.stop();
  }

  @Test(expected = IllegalStateException.class)
  public void execute_WithoutBeeingStared_ThrowIllegalStateException() throws Exception {

    SimpleOfficeManager.make().execute(new SimpleOfficeTask());
  }

  @Test(expected = IllegalStateException.class)
  public void execute_WhenTerminated_ThrowIllegalStateException() throws Exception {

    final SimpleOfficeManager manager = SimpleOfficeManager.make();
    try {
      manager.start();
    } finally {
      manager.stop();
    }

    manager.execute(new SimpleOfficeTask());
  }

  @Test
  public void execute_TaskQueueTimeout_ThrowOfficeException() throws Exception {

    final SimpleOfficeManager manager =
        SimpleOfficeManager.builder().taskQueueTimeout(1000L).build();
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
