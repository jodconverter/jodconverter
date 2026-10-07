/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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

package org.jodconverter.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link FileUtils} class. */
@SuppressWarnings("ResultOfMethodCallIgnored")
class FileUtilsTest {

  @Test
  void classWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(FileUtils.class);
  }

  @Nested
  class CopyDirectory {

    @Nested
    class Failure {

      @Test
      void whenSourceDoesNotExist_ShouldThrowIllegalArgumentException(
          final @TempDir File testFolder) {

        final var dir = new File(testFolder, "test");
        dir.mkdir();
        final var from = new File(dir, "from");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.copyDirectory(from, new File(dir, "to.txt")))
            .withMessage("srcDir must be an existing directory");
      }

      @Test
      void whenSourceIsFile_ShouldThrowIllegalArgumentException(final @TempDir File testFolder)
          throws IOException {

        final var dir = new File(testFolder, "test");
        dir.mkdir();
        final var from = new File(dir, "from.txt");
        from.createNewFile();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.copyDirectory(from, new File(dir, "to")))
            .withMessage("srcDir must be an existing directory");
      }

      @Test
      void whenTargetAlreadyExists_ShouldThrowFileAlreadyExistsException(
          final @TempDir File testFolder) {

        final var dir = new File(testFolder, "test");
        dir.mkdir();
        final var from = new File(dir, "from");
        from.mkdir();
        final var to = new File(dir, "to");
        to.mkdir();

        assertThatExceptionOfType(FileAlreadyExistsException.class)
            .isThrownBy(() -> FileUtils.copyDirectory(from, to));
      }

      @Test
      void whenTargetIsFile_ShouldThrowIllegalArgumentException(final @TempDir File testFolder)
          throws IOException {

        final var dir = new File(testFolder, "test");
        dir.mkdir();
        final var from = new File(dir, "from");
        from.mkdir();
        final var to = new File(dir, "to.txt");
        to.createNewFile();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.copyDirectory(from, to))
            .withMessage("destDir cannot be an existing file");
      }

      @Test
      void whenTargetIsChildOfSource_ShouldThrowIllagalArgumentException(
          final @TempDir File testFolder) {

        final var dir = new File(testFolder, "test");
        dir.mkdir();
        final var from = new File(dir, "from");
        from.mkdir();
        final var to = new File(from, "to");
        to.mkdir();
        final var to1 = new File(to, "to");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.copyDirectory(from, to1))
            .withMessage("destDir cannot be a child of srcDir");
      }
    }

    @Nested
    class Success {

      @Test
      void whenTargetDoesNotExist_ShouldCopyFileAndModifiedDate(final @TempDir File testFolder)
          throws IOException {

        final var encoding = StandardCharsets.UTF_8;

        final var from = new File(testFolder, "test");
        from.mkdir();
        final var from1 = new File(from, "from1.txt");
        from1.createNewFile();
        final var from2 = new File(from, "from2/from2.txt");
        from2.getParentFile().mkdirs();
        from2.createNewFile();
        final var from3 = new File(from, "from3/from3/from1.txt");
        from3.getParentFile().mkdirs();
        from3.createNewFile();
        final var toDir = new File(testFolder, "to");

        Files.writeString(from1.toPath(), "Whatever1", encoding);
        Files.writeString(from2.toPath(), "Whatever2", encoding);
        Files.writeString(from3.toPath(), "Whatever3", encoding);

        FileUtils.copyDirectory(from, toDir);

        final var to1 = new File(toDir, "from1.txt");
        assertThat(Files.readString(to1.toPath(), encoding)).isEqualTo("Whatever1");
        assertThat(from1.lastModified()).isEqualTo(to1.lastModified());
        final var to2 = new File(toDir, "from2/from2.txt");
        assertThat(Files.readString(to2.toPath(), encoding)).isEqualTo("Whatever2");
        assertThat(from2.lastModified()).isEqualTo(to2.lastModified());
        final var to3 = new File(toDir, "from3/from3/from1.txt");
        assertThat(Files.readString(to3.toPath(), encoding)).isEqualTo("Whatever3");
        assertThat(from3.lastModified()).isEqualTo(to3.lastModified());
      }

      @Test
      void whenTargetAlreadyExistsWithReplaceOption_ShouldCopyFilesAndModifiedDate(
          final @TempDir File testFolder) throws IOException {

        final var encoding = StandardCharsets.UTF_8;

        final var from = new File(testFolder, "test");
        from.mkdir();
        final var from1 = new File(from, "from1.txt");
        from1.createNewFile();
        final var from2 = new File(from, "from2/from2.txt");
        from2.getParentFile().mkdirs();
        from2.createNewFile();
        final var from3 = new File(from, "from3/from3/from1.txt");
        from3.getParentFile().mkdirs();
        from3.createNewFile();
        final var toDir = new File(testFolder, "to");
        toDir.mkdir();

        Files.writeString(from1.toPath(), "Whatever1", encoding);
        Files.writeString(from2.toPath(), "Whatever2", encoding);
        Files.writeString(from3.toPath(), "Whatever3", encoding);

        FileUtils.copyDirectory(from, toDir, StandardCopyOption.REPLACE_EXISTING);

        final var to1 = new File(toDir, "from1.txt");
        assertThat(Files.readString(to1.toPath(), encoding)).isEqualTo("Whatever1");
        assertThat(from1.lastModified()).isEqualTo(to1.lastModified());
        final var to2 = new File(toDir, "from2/from2.txt");
        assertThat(Files.readString(to2.toPath(), encoding)).isEqualTo("Whatever2");
        assertThat(from2.lastModified()).isEqualTo(to2.lastModified());
        final var to3 = new File(toDir, "from3/from3/from1.txt");
        assertThat(Files.readString(to3.toPath(), encoding)).isEqualTo("Whatever3");
        assertThat(from3.lastModified()).isEqualTo(to3.lastModified());
      }
    }
  }

  @Nested
  class Delete {

    @Nested
    class Failure {

      @Test
      void whenIOExceptionOccured_ShouldThrowIOException(final @TempDir File testFolder)
          throws IOException {

        // TODO: Find a way to make that test work on non-windows OS.
        assumeTrue(OSUtils.IS_OS_WINDOWS);

        final var dir = new File(testFolder, "test");
        dir.mkdir();
        final var file = new File(dir, "test.txt");
        file.createNewFile();

        // Use the file channel to create a lock on the file.
        // This method blocks until it can retrieve the lock.
        try (var channel = new RandomAccessFile(file, "rw").getChannel();
            var lock = channel.lock()) {
          assertThat(lock.isValid()).isTrue();

          // Call FileUtils.delete on the root directory. It should throw
          // an exception since we have a lock on the file.
          assertThatIOException().isThrownBy(() -> FileUtils.delete(dir));
        }
      }
    }

    @Nested
    class Success {

      @Test
      void withNull_ShouldReturnFalse() throws IOException {
        assertThat(FileUtils.delete(null)).isFalse();
      }

      @Test
      void withUnexistingFile_ShouldReturnFalse() throws IOException {
        assertThat(FileUtils.delete(new File(UUID.randomUUID().toString()))).isFalse();
      }

      @Test
      void withFolderNotEmpty_ShouldDeleteFolderRecursivelyAndReturnTrue(
          final @TempDir File testFolder) throws IOException {

        final var root = new File(testFolder, "test");
        root.mkdir();

        var dir = new File(root, "test1");
        dir.mkdir();
        var file = new File(dir, "test1.txt");
        file.createNewFile();
        file = new File(dir, "test2.txt");
        file.createNewFile();

        dir = new File(root, "test2");
        dir.mkdir();
        file = new File(dir, "test1.txt");
        file.createNewFile();
        file = new File(dir, "test2.txt");
        file.createNewFile();

        dir = new File(dir, "test3");
        dir.mkdir();
        file = new File(dir, "test1.txt");
        file.createNewFile();
        file = new File(dir, "test2.txt");
        file.createNewFile();

        dir = new File(dir, "test4");
        dir.mkdir();
        file = new File(dir, "test1.txt");
        file.createNewFile();
        file = new File(dir, "test2.txt");
        file.createNewFile();

        assertThat(FileUtils.delete(root)).isTrue();
      }
    }
  }

  @Nested
  class DeleteQuietly {

    @Test
    void whenIOExceptionOccured_ShouldSwallowIOException(final @TempDir File testFolder)
        throws IOException {

      final var dir = new File(testFolder, "test");
      dir.mkdir();
      final var file = new File(dir, "test.txt");
      file.createNewFile();

      try (var channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE);
          var lock = channel.lock()) {
        assertThat(lock.isValid()).isTrue();
        assertThatCode(() -> FileUtils.deleteQuietly(dir)).doesNotThrowAnyException();
      }
    }
  }

  @Nested
  class GetName {

    @Test
    void withNull_ShouldReturnNull() {
      assertThat(FileUtils.getName(null)).isNull();
    }

    @Test
    void withFullPath_ShouldReturnFileName() {
      assertThat(FileUtils.getName("a/b/c.txt")).isEqualTo("c.txt");
    }

    @Test
    void withOnlyFileName_ShouldReturnFileName() {
      assertThat(FileUtils.getName("c.txt")).isEqualTo("c.txt");
    }

    @Test
    void withFullPathWithoutExtension_ShouldReturnFileName() {
      assertThat(FileUtils.getName("a/b/c")).isEqualTo("c");
    }

    @Test
    void withOnlyFileNameWithoutExtension_ShouldReturnFileName() {
      assertThat(FileUtils.getName("c")).isEqualTo("c");
    }

    @Test
    void withFullDirectoryPath_ShouldReturnEmptyString() {
      assertThat(FileUtils.getName("a/b/")).isEqualTo("");
    }

    @Test
    void withSlash_ShouldReturnEmptyString() {
      assertThat(FileUtils.getName("/")).isEqualTo("");
    }
  }

  @Nested
  class GetBaseName {

    @Test
    void withNull_ShouldReturnNull() {
      assertThat(FileUtils.getBaseName(null)).isNull();
    }

    @Test
    void withFullPath_ShouldReturnBaseName() {
      assertThat(FileUtils.getBaseName("a/b/c.txt")).isEqualTo("c");
    }

    @Test
    void withOnlyFileName_ShouldReturnBaseName() {
      assertThat(FileUtils.getBaseName("c.txt")).isEqualTo("c");
    }

    @Test
    void withFullPathWithoutExtension_ShouldReturnFileName() {
      assertThat(FileUtils.getBaseName("a/b/c")).isEqualTo("c");
    }

    @Test
    void withOnlyFileNameWithoutExtension_ShouldReturnFileName() {
      assertThat(FileUtils.getBaseName("c")).isEqualTo("c");
    }

    @Test
    void withFullDirectoryPath_ShouldReturnEmptyString() {
      assertThat(FileUtils.getBaseName("a/b/")).isEqualTo("");
    }

    @Test
    void withSlash_ShouldReturnEmptyString() {
      assertThat(FileUtils.getBaseName("/")).isEqualTo("");
    }
  }

  @Nested
  class GetExtension {

    @Test
    void withNull_ShouldReturnNull() {
      assertThat(FileUtils.getExtension(null)).isNull();
    }

    @Test
    void withFullPath_ShouldReturnExtension() {
      assertThat(FileUtils.getExtension("a/b/c.txt")).isEqualTo("txt");
    }

    @Test
    void withOnlyFileName_ShouldReturnExtension() {
      assertThat(FileUtils.getExtension("c.txt")).isEqualTo("txt");
    }

    @Test
    void withFullPathWithoutExtension_ShouldReturnEmptyString() {
      assertThat(FileUtils.getExtension("a/b/c")).isEqualTo("");
    }

    @Test
    void withOnlyFileNameWithoutExtension_ShouldReturnEmptyString() {
      assertThat(FileUtils.getExtension("c")).isEqualTo("");
    }

    @Test
    void withFullDirectoryPath_ShouldReturnEmptyString() {
      assertThat(FileUtils.getExtension("a/b/")).isEqualTo("");
    }

    @Test
    void withSlash_ShouldReturnEmptyString() {
      assertThat(FileUtils.getExtension("/")).isEqualTo("");
    }

    @Test
    void withEmptyString_ShouldReturnEmptyString() {
      assertThat(FileUtils.getExtension("")).isEqualTo("");
    }

    @Test
    void withDot_ShouldReturnEmptyString() {
      assertThat(FileUtils.getExtension(".")).isEqualTo("");
    }

    @Test
    void withDotButNoExtension_ShouldReturnEmptyString() {
      assertThat(FileUtils.getExtension("/test/a.")).isEqualTo("");
    }

    @Test
    void withDotButNoBaseNameNorExtension_ShouldReturnEmptyString() {
      assertThat(FileUtils.getExtension("/test/.")).isEqualTo("");
    }
  }

  @Nested
  class GetFullPath {

    @Test
    void withNull_ShouldReturnNull() {
      assertThat(FileUtils.getBaseName(null)).isNull();
    }
  }
}
