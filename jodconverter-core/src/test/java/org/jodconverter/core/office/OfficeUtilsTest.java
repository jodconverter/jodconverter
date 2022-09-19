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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import org.jodconverter.core.test.util.AssertUtil;
import org.jodconverter.core.util.FileUtils;

/** Contains tests for the {@link OfficeUtils} class. */
class OfficeUtilsTest {

  @Test
  void classWellDefined() {
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

      assertThatCode(() -> OfficeUtils.stopQuietly(officeManager)).doesNotThrowAnyException();
    }

    @Test
    void withNull_ShouldDoNothing() {
      assertThatCode(() -> OfficeUtils.stopQuietly(null)).doesNotThrowAnyException();
    }

    @Test
    void withNotNull_ShouldCallClose() throws OfficeException {

      final OfficeManager officeManager = mock(OfficeManager.class);
      OfficeUtils.stopQuietly(officeManager);
      verify(officeManager, times(1)).stop();
    }
  }

  @Nested
  class deleteOrRename {
    @Test
    @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
    void whenCannotBeDeleted_ShouldRename() throws OfficeException, IOException {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      manager.start();

      final File mockDir = mock(File.class);
      final Path mockPath = mock(Path.class);
      final File tempDir = (File) ReflectionTestUtils.getField(manager, "tempDir");
      ReflectionTestUtils.setField(manager, "tempDir", mockDir);
      when(mockDir.exists()).thenAnswer(invocation -> tempDir.exists());
      when(mockDir.getName()).thenAnswer(invocation -> tempDir.getName());
      when(mockDir.getParentFile()).thenAnswer(invocation -> tempDir.getParentFile());
      when(mockDir.toPath()).thenAnswer(invocation -> mockPath);
      when(mockDir.renameTo(isA(File.class)))
          .thenAnswer(
              invocation -> {
                invocation.getArgument(0, File.class).createNewFile();
                return true;
              });

      final FileSystem mockFileSystem = mock(FileSystem.class);
      final FileSystemProvider mockFileSystemProvider = mock(FileSystemProvider.class);
      when(mockPath.getFileSystem()).thenAnswer(invocation -> mockFileSystem);
      when(mockFileSystem.provider()).thenAnswer(invocation -> mockFileSystemProvider);
      when(mockFileSystemProvider.readAttributes(isA(Path.class), any(Class.class), any()))
          .thenThrow(new IOException("So that Files.isDirectory(mockPath) return false"));
      doThrow(new IOException("You can't do that bud!"))
          .when(mockFileSystemProvider)
          .delete(isA(Path.class));

      manager.stop();
      final ArgumentCaptor<File> arg = ArgumentCaptor.forClass(File.class);
      verify(mockDir, times(1)).renameTo(arg.capture());
      assertThat(arg.getValue()).exists();
      assertThat(arg.getValue().getName().startsWith(tempDir.getName() + ".old."));
      assertThat(tempDir).exists();
      FileUtils.delete(tempDir);
      FileUtils.delete(arg.getValue());
    }

    @Test
    @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
    void whenCannotBeDeletedNorRenamed_ShouldNotRename() throws OfficeException, IOException {

      final SimpleOfficeManager manager = SimpleOfficeManager.make();
      manager.start();

      final File mockDir = mock(File.class);
      final Path mockPath = mock(Path.class);
      final File tempDir = (File) ReflectionTestUtils.getField(manager, "tempDir");
      ReflectionTestUtils.setField(manager, "tempDir", mockDir);
      when(mockDir.exists()).thenAnswer(invocation -> tempDir.exists());
      when(mockDir.getName()).thenAnswer(invocation -> tempDir.getName());
      when(mockDir.getParentFile()).thenAnswer(invocation -> tempDir.getParentFile());
      when(mockDir.toPath()).thenAnswer(invocation -> mockPath);
      when(mockDir.renameTo(isA(File.class))).thenAnswer(invocation -> false);

      final FileSystem mockFileSystem = mock(FileSystem.class);
      final FileSystemProvider mockFileSystemProvider = mock(FileSystemProvider.class);
      when(mockPath.getFileSystem()).thenAnswer(invocation -> mockFileSystem);
      when(mockFileSystem.provider()).thenAnswer(invocation -> mockFileSystemProvider);
      when(mockFileSystemProvider.readAttributes(isA(Path.class), any(Class.class), any()))
          .thenThrow(new IOException("So that Files.isDirectory(mockPath) return false"));
      doThrow(new IOException("You can't do that bud!"))
          .when(mockFileSystemProvider)
          .delete(isA(Path.class));

      manager.stop();
      final ArgumentCaptor<File> arg = ArgumentCaptor.forClass(File.class);
      verify(mockDir, times(1)).renameTo(arg.capture());
      assertThat(arg.getValue()).doesNotExist();
      assertThat(arg.getValue().getName().startsWith(tempDir.getName() + ".old."));
      assertThat(tempDir).exists();
      FileUtils.delete(tempDir);
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void whenCannotBeDeletedButCanBeRenamed_ShouldRenameDirectory(final @TempDir File testFolder)
        throws Exception {

      final File workingDir =
          new File(testFolder, "delete_WhenCannotBeDeletedButCanBeRenamed_ShouldRenameDirectory");
      workingDir.mkdirs();
      final File dirToDelete =
          new File(workingDir, "delete_WhenCannotBeDeletedButCanBeRenamed_ShouldRenameDirectory");
      dirToDelete.mkdirs();
      File.createTempFile("test1_", ".tmp", dirToDelete);
      File.createTempFile("test2_", ".tmp", dirToDelete);
      File.createTempFile("test3_", ".tmp", dirToDelete);
      File.createTempFile("test4_", ".tmp", dirToDelete);
      final File subDirToDelete = new File(dirToDelete, "foo");
      subDirToDelete.mkdirs();
      File.createTempFile("test1_", ".tmp", subDirToDelete);
      File.createTempFile("test2_", ".tmp", subDirToDelete);
      File.createTempFile("test3_", ".tmp", subDirToDelete);
      File.createTempFile("test4_", ".tmp", subDirToDelete);

      try (MockedStatic<FileUtils> utils = Mockito.mockStatic(FileUtils.class)) {
        utils.when(() -> FileUtils.delete(isA(File.class))).thenThrow(IOException.class);

        OfficeUtils.deleteOrRenameFile(dirToDelete, 0L, 0L);

        assertThat(
                workingDir.listFiles(
                    pathname ->
                        pathname.isDirectory()
                            && pathname.getName().startsWith(dirToDelete.getName() + ".old.")))
            .hasSize(1);
      }
    }

    @Test
    void withNull_ShouldDoNothing() {
      assertThatCode(() -> OfficeUtils.stopQuietly(null)).doesNotThrowAnyException();
    }

    @Test
    void withNotNull_ShouldCallClose() throws OfficeException {

      final OfficeManager officeManager = mock(OfficeManager.class);
      OfficeUtils.stopQuietly(officeManager);
      verify(officeManager, times(1)).stop();
    }
  }
}
