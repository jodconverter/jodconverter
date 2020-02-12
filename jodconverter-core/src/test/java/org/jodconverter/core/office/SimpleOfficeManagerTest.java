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

package org.jodconverter.core.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

/** Contains tests for the {@link SimpleOfficeManager} class. */
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
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager = SimpleOfficeManager.make();

    assertThat(manager).isInstanceOf(SimpleOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

    final List<OfficeManager> entries = Whitebox.getInternalState(manager, "entries");
    assertThat(entries).hasSize(1);
    entries.forEach(
        officeManager ->
            assertThat(officeManager)
                .isInstanceOf(SimpleOfficeManagerPoolEntry.class)
                .extracting("taskExecutionTimeout")
                .isEqualTo(120_000L));
  }

  @Test
  public void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues() {

    final OfficeManager manager =
        SimpleOfficeManager.builder()
            .workingDir(OfficeUtils.getDefaultWorkingDir())
            .taskExecutionTimeout(20_000L)
            .taskQueueTimeout(1_000L)
            .poolSize(2)
            .build();

    assertThat(manager).isInstanceOf(SimpleOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 1_000L);

    final List<OfficeManager> entries = Whitebox.getInternalState(manager, "entries");
    assertThat(entries).hasSize(2);
    entries.forEach(
        officeManager ->
            assertThat(officeManager)
                .isInstanceOf(SimpleOfficeManagerPoolEntry.class)
                .extracting("taskExecutionTimeout")
                .isEqualTo(20_000L));
  }

  @Test
  public void build_WithValuesAsString_ShouldInitializedOfficeManagerWithCustomValues() {

    final OfficeManager manager =
        SimpleOfficeManager.builder()
            .workingDir(OfficeUtils.getDefaultWorkingDir().getPath())
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    assertThat(manager).extracting("workingDir").isEqualTo(OfficeUtils.getDefaultWorkingDir());
  }

  @Test
  public void build_WithEmptyValuesAsString_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager = SimpleOfficeManager.builder().workingDir("   ").build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    assertThat(manager).extracting("workingDir").isEqualTo(OfficeUtils.getDefaultWorkingDir());
  }

  @Test
  public void start_StartTwice_ShouldThrowIllegalStateException() throws OfficeException {

    final SimpleOfficeManager manager = SimpleOfficeManager.make();
    try {
      manager.start();
      assertThatIllegalStateException().isThrownBy(manager::start);
    } finally {
      manager.stop();
    }
  }

  @Test
  public void start_WhenTerminated_ShouldThrowIllegalStateException() throws OfficeException {

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
  public void execute_WithoutBeingStared_ShouldThrowIllegalStateException() {

    assertThatIllegalStateException()
        .isThrownBy(() -> SimpleOfficeManager.make().execute(new SimpleOfficeTask()));
  }

  @Test
  public void execute_WhenTerminated_ShouldThrowIllegalStateException() throws OfficeException {

    final SimpleOfficeManager manager = SimpleOfficeManager.make();
    try {
      manager.start();
    } finally {
      manager.stop();
    }

    assertThatIllegalStateException().isThrownBy(() -> manager.execute(new SimpleOfficeTask()));
  }

  @Test
  public void execute_TaskQueueTimeout_ShouldThrowOfficeException()
      throws OfficeException, InterruptedException {

    final SimpleOfficeManager manager =
        SimpleOfficeManager.builder().taskQueueTimeout(1_000L).build();
    try {
      manager.start();

      // Create threads that will both execute a task taking more than a seconds to execute.
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
