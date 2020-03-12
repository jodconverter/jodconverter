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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.local.process.ProcessManager;
import org.jodconverter.local.process.ProcessQuery;

/** Contains tests for the {@link OfficeProcess} class. */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FileUtils.class)
public class OfficeProcessTest {

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  /**
   * Creates the test folder.
   *
   * @throws IOException If an IO error occurs.
   */
  @BeforeClass
  public static void setUpClass() throws IOException {

    // PowerMock reloads a test class with custom class loader
    // it is done after jUnit applies @ClassRule, so we have to
    // do this.
    // See https://github.com/powermock/powermock/issues/687
    testFolder.create();
  }

  /** Deletes the test folder. */
  @AfterClass
  public static void tearDownClass() {

    testFolder.delete();
  }

  @Test
  public void checkForExistingProcess_WhenIoExceptionCatched_TrowsOfficeException() {

    final OfficeProcess process =
        new OfficeProcess(
            new OfficeUrl(2002),
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            new ProcessManager() {
              @Override
              public void kill(final Process process, final long pid) throws IOException {
                throw new IOException();
              }

              @Override
              @SuppressWarnings("NullableProblems")
              public long findPid(final ProcessQuery query) throws IOException {
                throw new IOException();
              }
            },
            null,
            null,
            null);

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(
            () ->
                Whitebox.invokeMethod(
                    process,
                    "checkForExistingProcess",
                    new ProcessQuery("command", "acceptString")))
        .withMessage(
            "Could not check if there is already an existing process with --accept 'acceptString'")
        .withCauseExactlyInstanceOf(IOException.class);
  }

  @Test
  public void deleteProfileDir_WhenCannotBeDeletedButCanBeRenamed_DirectoryIRenamed()
      throws Exception {

    mockStatic(FileUtils.class);

    final File workingDir =
        testFolder.newFolder("deleteProfileDir_WhenCannotBeDeleted_RenameDirectory");

    doThrow(new IOException()).when(FileUtils.class, "delete", isA(File.class));

    final OfficeProcess process =
        new OfficeProcess(
            new OfficeUrl(2002),
            LocalOfficeUtils.getDefaultOfficeHome(),
            workingDir,
            LocalOfficeUtils.findBestProcessManager(),
            null,
            null,
            null);

    final File instanceProfileDir = Whitebox.getInternalState(process, "instanceProfileDir");
    //noinspection ResultOfMethodCallIgnored
    instanceProfileDir.mkdirs();
    Whitebox.invokeMethod(process, "deleteInstanceProfileDir");

    assertThat(
            workingDir.listFiles(
                pathname ->
                    pathname.isDirectory()
                        && pathname.getName().startsWith(instanceProfileDir.getName() + ".old.")))
        .hasSize(1);

    FileUtils.deleteQuietly(workingDir);
  }

  @Test
  public void deleteProfileDir_WhenCannotBeDeleted_OperationIgnored() throws Exception {

    mockStatic(FileUtils.class);

    final File workingDir =
        testFolder.newFolder("deleteProfileDir_WhenCannotBeDeleted_OperationIgnored");

    doThrow(new IOException()).when(FileUtils.class, "delete", isA(File.class));

    final OfficeProcess process =
        new OfficeProcess(
            new OfficeUrl(2002),
            LocalOfficeUtils.getDefaultOfficeHome(),
            workingDir,
            LocalOfficeUtils.findBestProcessManager(),
            null,
            null,
            null);

    final File instanceProfileDir = Whitebox.getInternalState(process, "instanceProfileDir");
    Whitebox.invokeMethod(process, "deleteInstanceProfileDir");

    assertThat(
            workingDir.listFiles(
                pathname ->
                    pathname.isDirectory()
                        && pathname.getName().startsWith(instanceProfileDir.getName() + ".old.")))
        .isNullOrEmpty();
  }

  @Test
  public void forciblyTerminate_WhenIoExceptionCatched_ShouldLogError() {

    final OfficeProcess process =
        new OfficeProcess(
            new OfficeUrl(2002),
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            new ProcessManager() {
              @Override
              public void kill(final Process process, final long pid) throws IOException {
                throw new IOException();
              }
            },
            null,
            null,
            null);

    // TODO: Check that the error message if properly logged.
    assertThatCode(
            () -> {
              final VerboseProcess verboseProcess = mock(VerboseProcess.class);
              Whitebox.setInternalState(process, "pid", 0L);
              Whitebox.setInternalState(process, "process", verboseProcess);
              process.forciblyTerminate();
            })
        .doesNotThrowAnyException();
  }

  @Test
  public void forciblyTerminate_WhenNotStarted_ShouldDoNothing() {

    final OfficeProcess process =
        new OfficeProcess(
            new OfficeUrl(2002),
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            LocalOfficeUtils.findBestProcessManager(),
            null,
            null,
            null);

    assertThatCode(process::forciblyTerminate).doesNotThrowAnyException();
  }
}
