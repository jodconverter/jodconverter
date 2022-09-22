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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import org.jodconverter.core.task.SimpleOfficeTask;

/** Contains tests for the {@link AbstractOfficeManagerPool} class. */
class AbstractOfficeManagerPoolTest {

  static final Logger LOGGER = LoggerFactory.getLogger(AbstractOfficeManagerPoolTest.class);

  private static class SleepyOfficeTaskRunner implements Runnable {

    private final OfficeManager manager;
    private final long sleep;
    OfficeException exception;

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
        LOGGER.error("Error", exception);
      }
    }
  }

  @Test
  void install_ShouldSetInstalledOfficeManagerHolder() {

    // Ensure we do not replace the current installed manager
    final OfficeManager installedManager = InstalledOfficeManagerHolder.getInstance();
    try {
      final OfficeManager manager = SimpleOfficeManager.install();
      assertThat(InstalledOfficeManagerHolder.getInstance()).isEqualTo(manager);
    } finally {
      InstalledOfficeManagerHolder.setInstance(installedManager);
    }
  }

  @Nested
  class Build {

    @Test
    void withDefaultValues_ShouldCreateOfficeManagerWithDefaultValues() {

      final OfficeManager manager = SimpleOfficeManager.make();

      assertThat(manager).isInstanceOf(SimpleOfficeManager.class);
      assertThat(manager)
          .extracting("tempDir.parentFile", "taskQueueTimeout")
          .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

      assertThat(manager)
          .extracting("entries")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .isInstanceOf(SimpleOfficeManagerPoolEntry.class)
                      .extracting("taskExecutionTimeout")
                      .isEqualTo(120_000L));
    }

    @Test
    void withNullValues_ShouldCreateOfficeManagerWithDefaultValues() {

      final OfficeManager manager =
          SimpleOfficeManager.builder()
              .workingDir((File) null)
              .workingDir((String) null)
              .taskExecutionTimeout(null)
              .taskQueueTimeout(null)
              .poolSize(null)
              .build();

      assertThat(manager).isInstanceOf(SimpleOfficeManager.class);
      assertThat(manager)
          .extracting("tempDir.parentFile", "taskQueueTimeout")
          .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

      assertThat(manager)
          .extracting("entries")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .isInstanceOf(SimpleOfficeManagerPoolEntry.class)
                      .extracting("taskExecutionTimeout")
                      .isEqualTo(120_000L));
    }

    @Test
    void withCustomValues_ShouldCreateOfficeManagerWithCustomValues() {

      final OfficeManager manager =
          SimpleOfficeManager.builder()
              .workingDir(OfficeUtils.getDefaultWorkingDir())
              .taskExecutionTimeout(20_000L)
              .taskQueueTimeout(1_000L)
              .poolSize(2)
              .build();

      assertThat(manager).isInstanceOf(SimpleOfficeManager.class);
      assertThat(manager)
          .extracting("tempDir.parentFile", "taskQueueTimeout")
          .containsExactly(OfficeUtils.getDefaultWorkingDir(), 1_000L);

      assertThat(manager)
          .extracting("entries")
          .asList()
          .hasSize(2)
          .allSatisfy(
              o ->
                  assertThat(o)
                      .isInstanceOf(SimpleOfficeManagerPoolEntry.class)
                      .extracting("taskExecutionTimeout")
                      .isEqualTo(20_000L));
    }

    @Test
    void withStringValues_ShouldCreateOfficeManagerUsingStringValues(
        final @TempDir File testFolder) {

      final OfficeManager manager =
          SimpleOfficeManager.builder().workingDir(testFolder.getPath()).build();

      assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
      assertThat(manager).extracting("tempDir.parentFile").isEqualTo(testFolder);
    }

    @Test
    void withEmptyStringValues_ShouldCreateOfficeManagerWithDefaultValues() {

      final OfficeManager manager = SimpleOfficeManager.builder().workingDir("   ").build();

      assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
      assertThat(manager)
          .extracting("tempDir.parentFile")
          .isEqualTo(OfficeUtils.getDefaultWorkingDir());
    }

    @Test
    void withNegativeTaskExecutionTimeout_ShouldThrowIllagalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> SimpleOfficeManager.builder().taskExecutionTimeout(-1L).build())
          .withMessage("taskExecutionTimeout -1 must greater than or equal to 0");
    }

    @Test
    void withNegativeTaskQueueTimeout_ShouldThrowIllagalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> SimpleOfficeManager.builder().taskQueueTimeout(-1L).build())
          .withMessage("taskQueueTimeout -1 must greater than or equal to 0");
    }
  }

  @Nested
  class Start {

    @Test
    void whenAlreadyStarted_ShouldThrowIllegalStateException() throws OfficeException {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      try {
        manager.start();
        assertThatIllegalStateException()
            .isThrownBy(manager::start)
            .withMessage("This office manager is already running.");
      } finally {
        manager.stop();
      }
    }

    @Test
    void whenTerminated_ShouldThrowIllegalStateException() throws OfficeException {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      manager.start();
      manager.stop();
      assertThatIllegalStateException()
          .isThrownBy(manager::start)
          .withMessage("This office manager has been shutdown.");
    }

    @Test
    void onceStarted_ShouldHaveStartedEntries() throws OfficeException {

      final SimpleOfficeManager manager = SimpleOfficeManager.builder().poolSize(5).build();
      try {
        manager.start();
        assertThat(manager)
            .extracting("entries")
            .asList()
            .hasSize(5)
            .allSatisfy(
                o ->
                    assertThat(o)
                        .isInstanceOf(SimpleOfficeManagerPoolEntry.class)
                        .extracting("taskExecutor.available")
                        .isEqualTo(true));
        assertThat(manager.isRunning()).isTrue();
      } finally {
        manager.stop();
      }
    }

    @Test
    void onceStarted_ShouldHaveCreatedTempDir() throws OfficeException {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      try {
        final File tempDir = (File) ReflectionTestUtils.getField(manager, "tempDir");
        assertThat(tempDir).doesNotExist();
        manager.start();
        assertThat(tempDir).isDirectory();
      } finally {
        manager.stop();
      }
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void whenTempDirAlreadyExists_ShouldHaveDeletesFirstThenCreatedTempDir()
        throws OfficeException, IOException {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      try {
        final File tempDir = (File) ReflectionTestUtils.getField(manager, "tempDir");
        tempDir.mkdirs();
        final File tempFile = new File(tempDir, "test.txt");
        assertThat(tempFile.createNewFile()).isTrue();
        assertThat(tempDir).isDirectory();
        assertThat(tempDir.listFiles()).hasSize(1);
        manager.start();
        assertThat(tempDir).isDirectory();
        assertThat(tempDir.listFiles()).isEmpty();
      } finally {
        manager.stop();
      }
    }

    @Test
    void whenTempDirNotCreated_ShouldThrowOfficeException() throws OfficeException {

      final File mockDir = mock(File.class);
      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      try {
        ReflectionTestUtils.setField(manager, "tempDir", mockDir);
        when(mockDir.exists()).thenAnswer(invocation -> false);
        when(mockDir.mkdirs()).thenAnswer(invocation -> true);
        when(mockDir.isDirectory()).thenAnswer(invocation -> false);
        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(manager::start)
            .withMessageStartingWith("Cannot create temporary directory");
      } finally {
        manager.stop();
      }
    }
  }

  @Nested
  class Stop {

    @Test
    void whenAlreadyTerminated_SubsequentStopIgnored() throws OfficeException {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      manager.start();
      manager.stop();
      assertThatCode(manager::stop).doesNotThrowAnyException();
    }

    @Test
    void onceTerminated_ShouldHaveStoppedEntries() throws OfficeException {

      final SimpleOfficeManager manager = SimpleOfficeManager.builder().poolSize(5).build();
      manager.start();
      manager.stop();
      assertThat(manager)
          .extracting("entries")
          .asList()
          .hasSize(5)
          .allSatisfy(
              o ->
                  assertThat(o)
                      .isInstanceOf(SimpleOfficeManagerPoolEntry.class)
                      .extracting("taskExecutor.available")
                      .isEqualTo(false));
    }
  }

  @Nested
  class IsRunning {

    @Test
    void whenNotStartedYet_ShouldReturnFalse() {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      assertThat(manager.isRunning()).isEqualTo(false);
    }

    @Test
    void whenStarted_ShouldReturnTrue() throws OfficeException {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      manager.start();
      assertThat(manager.isRunning()).isEqualTo(true);
      manager.stop();
    }

    @Test
    void whenTerminated_ShouldReturnFalse() throws OfficeException {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      manager.start();
      manager.stop();
      assertThat(manager.isRunning()).isEqualTo(false);
    }
  }

  @Nested
  class Execute {

    @Test
    void whenNotStartedYet_ShouldThrowIllegalStateException() {

      assertThatIllegalStateException()
          .isThrownBy(() -> SimpleOfficeManager.make().execute(new SimpleOfficeTask()))
          .withMessage("This office manager is not running.");
    }

    @Test
    void whenTerminated_ShouldThrowIllegalStateException() throws OfficeException {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      manager.start();
      manager.stop();

      assertThatIllegalStateException().isThrownBy(() -> manager.execute(new SimpleOfficeTask()));
    }

    @Test
    void whenTaskQueueTimeout_ShouldThrowOfficeException()
        throws OfficeException, InterruptedException {

      final SimpleOfficeManager manager =
          SimpleOfficeManager.builder().taskQueueTimeout(500L).build();
      try {
        manager.start();

        // Create threads that will both execute a task taking more than a seconds to execute.
        final SleepyOfficeTaskRunner runnable1 = new SleepyOfficeTaskRunner(manager, 1_000L);
        final SleepyOfficeTaskRunner runnable2 = new SleepyOfficeTaskRunner(manager, 500L);
        final Thread thread1 = new Thread(runnable1);
        final Thread thread2 = new Thread(runnable2);

        // Start the threads.
        thread1.start();
        Thread.sleep(250L);

        thread2.start();
        Thread.sleep(250L);

        // Wait for thread to complete
        thread1.join();
        thread2.join();

        // Here, the second runnable should contain the task queue timeout exception
        assertThat(runnable1.exception).isNull();
        assertThat(runnable2.exception)
            .isExactlyInstanceOf(OfficeException.class)
            .hasMessage("No office manager available after 500 millisec");

      } finally {
        manager.stop();
      }
    }

    //    @Test
    //    void whenInterruptedWhileAquiringManager_ShouldThrowOfficeException()
    //        throws OfficeException, InterruptedException {
    //
    //      final SimpleOfficeManager manager =
    //          SimpleOfficeManager.builder().taskQueueTimeout(500L).build();
    //      try {
    //        manager.start();
    //
    //        // Create threads that will both execute a task taking more than a seconds to execute.
    //        final SleepyOfficeTaskRunner runnable1 = new SleepyOfficeTaskRunner(manager, 1_000L);
    //        final SleepyOfficeTaskRunner runnable2 = new SleepyOfficeTaskRunner(manager, 500L);
    //        final Thread thread1 = new Thread(runnable1);
    //        final Thread thread2 = new Thread(runnable2);
    //
    //        // Start the threads.
    //        thread1.start();
    //        Thread.sleep(250L);
    //
    //        thread2.start();
    //        Thread.sleep(250L);
    //
    //        // Interrupt the second thread
    //        thread2.interrupt();
    //
    //        // Wait for threads to complete
    //        thread1.join();
    //        thread2.join();
    //
    //        // Here, the second runnable should contain the interruption exception
    //        assertThat(runnable2.exception)
    //            .isExactlyInstanceOf(OfficeException.class)
    //            .hasMessage("Interruption while acquiring manager")
    //            .hasCauseExactlyInstanceOf(InterruptedException.class);
    //
    //      } finally {
    //        manager.stop();
    //      }
    //    }

    @Test
    void whenInterruptedWhileReleasingAquiringManager_ShouldThrowOfficeException()
        throws Exception {

      final SimpleOfficeManager manager =
          SimpleOfficeManager.builder().taskQueueTimeout(2_500L).build();
      try {
        manager.start();

        final AtomicReference<Throwable> ex = new AtomicReference<>();

        assertThatCode(
                () -> {
                  final Thread thread =
                      new Thread(
                          () -> {
                            try {
                              // Try to release a fake entry while there is no place in the manager
                              ReflectionTestUtils.invokeMethod(
                                  manager,
                                  "releaseManager",
                                  new SimpleOfficeManagerPoolEntry(1_000L));
                            } catch (UndeclaredThrowableException e) {
                              ex.set(e.getUndeclaredThrowable());
                            }
                          });

                  // Start the thread.
                  thread.start();
                  // Interrupt the thread.
                  thread.interrupt();
                  //  Wait for thread to complete.
                  thread.join();
                })
            .doesNotThrowAnyException();

        assertThat(ex.get())
            .isExactlyInstanceOf(OfficeException.class)
            .hasMessage("Interruption while releasing manager")
            .hasCauseExactlyInstanceOf(InterruptedException.class);

      } finally {
        manager.stop();
      }
    }
  }

  @Nested
  class MakeTemporaryFile {

    @Test
    @SuppressWarnings("ConstantConditions")
    void withoutArgument_ShouldCreateTempFileWithoutExtension()
        throws OfficeException, IOException {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      manager.start();
      assertThat(manager.makeTemporaryFile().createNewFile()).isTrue();
      assertThat(manager)
          .extracting("tempDir")
          .isInstanceOfSatisfying(
              File.class,
              file -> {
                final File[] files = file.listFiles();
                assertThat(files).hasSize(1);
                //noinspection ConstantConditions
                assertThat(files[0].getName().indexOf('.')).isEqualTo(-1);
              });
      manager.stop();
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void withBlankExtension_ShouldCreateTempFileWithoutExtension()
        throws OfficeException, IOException {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      manager.start();
      assertThat(manager.makeTemporaryFile("   ").createNewFile()).isTrue();
      assertThat(manager)
          .extracting("tempDir")
          .isInstanceOfSatisfying(
              File.class,
              file -> {
                final File[] files = file.listFiles();
                assertThat(files).hasSize(1);
                assertThat(files[0].getName().indexOf('.')).isEqualTo(-1);
              });
      manager.stop();
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void withExtension_ShouldCreateTempFileWithExtension() throws OfficeException, IOException {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      manager.start();
      assertThat(manager.makeTemporaryFile("txt").createNewFile()).isTrue();
      assertThat(manager)
          .extracting("tempDir")
          .isInstanceOfSatisfying(
              File.class,
              file -> {
                final File[] files = file.listFiles();
                assertThat(files).hasSize(1);
                assertThat(files[0]).hasExtension("txt");
              });
      manager.stop();
    }
  }
}
