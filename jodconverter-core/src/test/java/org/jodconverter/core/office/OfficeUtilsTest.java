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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link OfficeUtils} class. */
class OfficeUtilsTest {

  @Test
  void new_ClassWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(OfficeUtils.class);
  }

  @Nested
  class GetDefaultWorkingDir {

    @Test
    void whenNotCustomTempDir_ShouldReturnDefaultTempDir() {

      final File defaultTempDir = new File(System.getProperty("java.io.tmpdir"));
      assertThat(defaultTempDir).isEqualTo(OfficeUtils.getDefaultWorkingDir());
    }

    @Test
    void whenCustomTempDir_ShouldReturnCustomTempDir(final @TempDir File testFolder) {

      final String backup = System.getProperty("java.io.tmpdir");
      try {
        System.setProperty("java.io.tmpdir", testFolder.getAbsolutePath());
        assertThat(OfficeUtils.getDefaultWorkingDir()).isEqualTo(testFolder);
      } finally {
        System.setProperty("java.io.tmpdir", backup);
      }
    }
  }

  @Nested
  class ValidateWorkingDir {

    @Test
    void whenNotExists_ShouldThrowIllegalStateException(final @TempDir File testFolder) {

      final File workingDir = new File(testFolder, "temp");
      assertThatExceptionOfType(IllegalStateException.class)
          .isThrownBy(() -> OfficeUtils.validateWorkingDir(workingDir))
          .withMessageStartingWith("workingDir doesn't exist or is not a directory");
    }

    @Test
    void whenIsFile_ShouldThrowIllegalStateException(final @TempDir File testFolder)
        throws IOException {

      final File file = new File(testFolder, getClass().getName() + ".txt");
      assertThat(file.createNewFile()).isTrue();
      assertThatExceptionOfType(IllegalStateException.class)
          .isThrownBy(() -> OfficeUtils.validateWorkingDir(file))
          .withMessageStartingWith("workingDir doesn't exist or is not a directory");
    }

    @Test
    void whenNotWritable_ShouldThrowIllegalStateException() {

      final File workingDir = mock(File.class);
      when(workingDir.isDirectory()).thenAnswer(invocation -> true);
      when(workingDir.canWrite()).thenAnswer(invocation -> false);

      assertThatExceptionOfType(IllegalStateException.class)
          .isThrownBy(() -> OfficeUtils.validateWorkingDir(workingDir))
          .withMessageEndingWith("cannot be written to");
    }

    @Test
    void whenDirectoryAndWritable_ShouldNotThrowAnyException() {

      final File workingDir = mock(File.class);
      when(workingDir.isDirectory()).thenAnswer(invocation -> true);
      when(workingDir.canWrite()).thenAnswer(invocation -> true);

      assertThatCode(() -> OfficeUtils.validateWorkingDir(workingDir)).doesNotThrowAnyException();
    }
  }

  @Nested
  class StopQuietly {
    @Test
    void whenOfficeExceptionThrown_ShouldSwallowException() throws OfficeException {

      final OfficeManager officeManager = mock(OfficeManager.class);
      doThrow(OfficeException.class).when(officeManager).stop();

      Assertions.assertThatCode(() -> OfficeUtils.stopQuietly(officeManager))
          .doesNotThrowAnyException();
    }

    @Test
    void withNull_ShouldDoNothing() {
      Assertions.assertThatCode(() -> OfficeUtils.stopQuietly(null)).doesNotThrowAnyException();
    }

    @Test
    void withNotNull_ShouldCallClose() throws OfficeException {

      final OfficeManager officeManager = mock(OfficeManager.class);
      OfficeUtils.stopQuietly(officeManager);
      verify(officeManager, times(1)).stop();
    }
  }
}
