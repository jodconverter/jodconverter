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

package org.jodconverter.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

public class SimpleOfficeManagerTest {

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
      final OfficeManager manager = SimpleOfficeManager.install();
      assertThat(InstalledOfficeManagerHolder.getInstance()).isEqualTo(manager);
    } finally {
      InstalledOfficeManagerHolder.setInstance(installedManager);
    }
  }

  @Test
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues()
      throws OfficeException {

    final OfficeManager manager = SimpleOfficeManager.make();

    assertThat(manager).isInstanceOf(SimpleOfficeManager.class);
    final SimpleOfficeManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getTaskExecutionTimeout()).isEqualTo(120000L);
    assertThat(config.getTaskQueueTimeout()).isEqualTo(30000L);

    manager.start();
    try {
      final OfficeManager[] poolEntries = Whitebox.getInternalState(manager, "entries");
      assertThat(poolEntries).hasSize(1);
      assertThat(poolEntries[0]).isInstanceOf(SimpleOfficeManagerPoolEntry.class);
    } finally {
      manager.stop();
    }
  }

  @Test
  public void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues()
      throws OfficeException {

    final OfficeManager manager =
        SimpleOfficeManager.builder()
            .workingDir(System.getProperty("java.io.tmpdir"))
            .taskExecutionTimeout(20000)
            .taskQueueTimeout(1000)
            .poolSize(2)
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final SimpleOfficeManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getTaskExecutionTimeout()).isEqualTo(20000L);
    assertThat(config.getTaskQueueTimeout()).isEqualTo(1000L);

    manager.start();
    try {
      final OfficeManager[] poolEntries = Whitebox.getInternalState(manager, "entries");
      assertThat(poolEntries).hasSize(2);
      assertThat(poolEntries[0]).isInstanceOf(SimpleOfficeManagerPoolEntry.class);
    } finally {
      manager.stop();
    }
  }

  @Test
  public void build_WithValuesAsString_ShouldInitializedOfficeManagerWithCustomValues() {

    final OfficeManager manager =
        SimpleOfficeManager.builder()
            .workingDir(new File(System.getProperty("java.io.tmpdir")).getPath())
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final SimpleOfficeManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
  }

  @Test
  public void build_WithEmptyValuesAsString_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager = SimpleOfficeManager.builder().workingDir("   ").build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final SimpleOfficeManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
  }

  @Test
  public void start_StartTwice_ThrowIllegalStateException() throws OfficeException {

    final SimpleOfficeManager manager = SimpleOfficeManager.make();
    try {
      manager.start();
      assertThatIllegalStateException().isThrownBy(manager::start);
    } finally {
      manager.stop();
    }
  }

  @Test
  public void start_WhenTerminated_ThrowIllegalStateException() throws OfficeException {

    final SimpleOfficeManager manager = SimpleOfficeManager.make();
    manager.start();
    manager.stop();
    assertThatIllegalStateException().isThrownBy(manager::start);
  }

  @Test
  public void stop_WhenTerminated_SecondStopIgnored() throws OfficeException {

    final SimpleOfficeManager manager = SimpleOfficeManager.make();
    manager.start();
    manager.stop();
    assertThatCode(manager::stop).doesNotThrowAnyException();
  }

  @Test
  public void execute_WithoutBeeingStared_ThrowIllegalStateException() {

    assertThatIllegalStateException()
        .isThrownBy(() -> SimpleOfficeManager.make().execute(new SimpleOfficeTask()));
  }

  @Test
  public void execute_WhenTerminated_ThrowIllegalStateException() throws OfficeException {

    final SimpleOfficeManager manager = SimpleOfficeManager.make();
    try {
      manager.start();
    } finally {
      manager.stop();
    }

    assertThatIllegalStateException().isThrownBy(() -> manager.execute(new SimpleOfficeTask()));
  }

  @Test
  public void execute_TaskQueueTimeout_ThrowOfficeException()
      throws OfficeException, InterruptedException {

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
