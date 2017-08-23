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
import static org.mockito.ArgumentMatchers.isA;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import org.jodconverter.process.ProcessManager;
import org.jodconverter.process.ProcessQuery;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FileUtils.class)
public class OfficeProcessTest {

  private static final String TEST_OUTPUT_DIR = "build/test-results/";

  private static File outputDir;

  /**
   * Redirects the console output and also changes the security manager so we can trap the exit code
   * of the application.
   */
  @BeforeClass
  public static void setUpClass() {

    outputDir = new File(TEST_OUTPUT_DIR, OfficeProcessTest.class.getSimpleName());
    outputDir.mkdirs();
  }

  /** Deletes the output directory once the tests are all done. */
  @AfterClass
  public static void tearDownClass() {

    // Delete the output directory
    FileUtils.deleteQuietly(outputDir);
  }

  @Test
  public void deleteProfileDir_WhenCannotBeDeletedButCanBeRenamed_DirectoryIRenamed()
      throws Exception {

    mockStatic(FileUtils.class);

    final File workingDir =
        new File(outputDir, "deleteProfileDir_WhenCannotBeDeleted_RenameDirectory");

    doThrow(new IOException()).when(FileUtils.class, "deleteDirectory", isA(File.class));

    final OfficeProcessConfig config = new OfficeProcessConfig(null, workingDir, null);
    final OfficeProcess process = new OfficeProcess(new OfficeUrl(2002), config);
    final File instanceProfileDir = Whitebox.invokeMethod(process, "getInstanceProfileDir");
    instanceProfileDir.mkdirs();
    Whitebox.invokeMethod(process, "deleteProfileDir");

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
        new File(outputDir, "deleteProfileDir_WhenCannotBeDeleted_OperationIgnored");

    doThrow(new IOException()).when(FileUtils.class, "deleteDirectory", isA(File.class));

    final OfficeProcessConfig config = new OfficeProcessConfig(null, workingDir, null);
    final OfficeProcess process = new OfficeProcess(new OfficeUrl(2002), config);
    final File instanceProfileDir = Whitebox.invokeMethod(process, "getInstanceProfileDir");
    Whitebox.invokeMethod(process, "deleteProfileDir");

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

    FileUtils.deleteQuietly(workingDir);
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

    try {
      process.forciblyTerminate(0L, 0L);
    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(OfficeException.class)
          .hasCauseExactlyInstanceOf(IOException.class);
    }
  }
}
