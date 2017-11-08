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
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import org.jodconverter.process.ProcessManager;
import org.jodconverter.process.ProcessQuery;

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

  /**
   * Deletes the test folder.
   *
   * @throws IOException If an IO error occurs.
   */
  @AfterClass
  public static void tearDownClass() {

    testFolder.delete();
  }

  @Test
  public void deleteProfileDir_WhenCannotBeDeletedButCanBeRenamed_DirectoryIRenamed()
      throws Exception {

    mockStatic(FileUtils.class);

    final File workingDir =
        testFolder.newFolder("deleteProfileDir_WhenCannotBeDeleted_RenameDirectory");

    doThrow(new IOException()).when(FileUtils.class, "deleteDirectory", isA(File.class));

    final OfficeProcessConfig config = new OfficeProcessConfig(null, workingDir, null);
    final OfficeProcess process = new OfficeProcess(new OfficeUrl(2002), config);
    final File instanceProfileDir = Whitebox.invokeMethod(process, "getInstanceProfileDir");
    instanceProfileDir.mkdirs();
    Whitebox.invokeMethod(process, "deleteInstanceProfileDir");

    assertThat(
            workingDir.listFiles(
                new FileFilter() {

                  @Override
                  public boolean accept(final File pathname) {
                    return pathname.isDirectory()
                        && StringUtils.startsWith(
                            pathname.getName(), instanceProfileDir.getName() + ".old.");
                  }
                }))
        .hasSize(1);

    FileUtils.deleteQuietly(workingDir);
  }

  @Test
  public void deleteProfileDir_WhenCannotBeDeleted_OperationIgnored() throws Exception {

    mockStatic(FileUtils.class);

    final File workingDir =
        testFolder.newFolder("deleteProfileDir_WhenCannotBeDeleted_OperationIgnored");

    doThrow(new IOException()).when(FileUtils.class, "deleteDirectory", isA(File.class));

    final OfficeProcessConfig config = new OfficeProcessConfig(null, workingDir, null);
    final OfficeProcess process = new OfficeProcess(new OfficeUrl(2002), config);
    final File instanceProfileDir = Whitebox.invokeMethod(process, "getInstanceProfileDir");
    Whitebox.invokeMethod(process, "deleteInstanceProfileDir");

    assertThat(
            workingDir.listFiles(
                new FileFilter() {

                  @Override
                  public boolean accept(final File pathname) {
                    return pathname.isDirectory()
                        && StringUtils.startsWith(
                            pathname.getName(), instanceProfileDir.getName() + ".old.");
                  }
                }))
        .isNullOrEmpty();
  }

  @Test
  public void forciblyTerminate_WhenIoExceptionCatched_TrowsOfficeException() throws Exception {

    final OfficeProcessConfig config = new OfficeProcessConfig(null, null, null);
    config.setProcessManager(
        new ProcessManager() {
          @Override
          public void kill(final Process process, final long pid) throws IOException {
            throw new IOException();
          }

          @Override
          public long findPid(final ProcessQuery query) throws IOException {
            return PID_NOT_FOUND;
          }
        });
    final OfficeProcess process = new OfficeProcess(new OfficeUrl(2002), config);

    final Process proc = mock(Process.class);
    try {
      Whitebox.setInternalState(process, "process", proc);
      process.forciblyTerminate(0L, 0L);
      fail("Exception expected");
    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(OfficeException.class)
          .hasCauseExactlyInstanceOf(IOException.class);
    }
  }

  @Test
  public void getExitCode_WhenNotStarted_ShouldReturn0() throws Exception {

    final OfficeProcessConfig config = new OfficeProcessConfig(null, null, null);
    final OfficeProcess process = new OfficeProcess(new OfficeUrl(2002), config);

    assertThat(process.getExitCode()).isEqualTo(0);
    assertThat(process.getExitCode(0L, 0L)).isEqualTo(0);
  }
}
