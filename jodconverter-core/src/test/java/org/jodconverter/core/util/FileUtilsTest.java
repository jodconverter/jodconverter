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

package org.jodconverter.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
  class CopyFile {

    @Nested
    class Failure {

      @Test
      void whenSourceIsDirectory_ShouldThrowIllegalArgumentException(
          final @TempDir File testFolder) {

        final File dir = new File(testFolder, "test");
        dir.mkdir();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.copyFile(dir, new File(dir, "to.txt")))
            .withMessage("srcFile must be an existing file");
      }

      @Test
      void whenSourceDoesNotExist_ShouldThrowIllegalArgumentException(
          final @TempDir File testFolder) {

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from.txt");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.copyFile(from, new File(dir, "to.txt")))
            .withMessage("srcFile must be an existing file");
      }

      @Test
      void whenTargetAlreadyExists_ShouldThrowFileAlreadyExistsException(
          final @TempDir File testFolder) throws IOException {

        final Charset encoding = StandardCharsets.UTF_8;

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from.txt");
        from.createNewFile();
        final File to = new File(dir, "to.txt");
        to.createNewFile();

        Files.write(from.toPath(), "Whatever".getBytes(encoding));
        Files.write(to.toPath(), "Yihaa".getBytes(encoding));

        assertThatExceptionOfType(FileAlreadyExistsException.class)
            .isThrownBy(() -> FileUtils.copyFile(from, to));

        assertThat(new String(Files.readAllBytes(to.toPath()), encoding)).isEqualTo("Yihaa");
      }
    }

    @Nested
    class Success {

      @Test
      void whenTargetDoesNotExist_ShouldCopyFileAndModifiedDate(final @TempDir File testFolder)
          throws IOException {

        final Charset encoding = StandardCharsets.UTF_8;
        final String test = "ABDCEF\nGHIJKL  \nMNOPQRS\n\tTUVWXYZééé^ç^ç^ç^ç^pawewew";

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from.txt");
        from.createNewFile();
        final File to = new File(dir, "to.txt");

        Files.write(from.toPath(), test.getBytes(encoding));

        FileUtils.copyFile(from, to);

        assertThat(new String(Files.readAllBytes(to.toPath()), encoding)).isEqualTo(test);
        assertThat(from.lastModified()).isEqualTo(to.lastModified());
      }

      @Test
      void whenTargetAlreadyExistsWithReplaceOption_ShouldCopyFileAndModifiedDate(
          final @TempDir File testFolder) throws IOException {

        final Charset encoding = StandardCharsets.UTF_8;

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from.txt");
        from.createNewFile();
        final File to = new File(dir, "to.txt");
        to.createNewFile();

        Files.write(from.toPath(), "Whatever".getBytes(encoding));
        Files.write(to.toPath(), "Yihaa".getBytes(encoding));

        FileUtils.copyFile(from, to, StandardCopyOption.REPLACE_EXISTING);

        assertThat(new String(Files.readAllBytes(to.toPath()), encoding)).isEqualTo("Whatever");
        assertThat(from.lastModified()).isEqualTo(to.lastModified());
      }
    }
  }

  @Nested
  class CopyFileToDirectory {

    @Nested
    class Failure {

      @Test
      void whenSourceIsDirectory_ShouldThrowIllegalArgumentException(
          final @TempDir File testFolder) {

        final File dir = new File(testFolder, "test");
        dir.mkdir();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.copyFileToDirectory(dir, new File(dir, "to")))
            .withMessage("srcFile must be an existing file");
      }

      @Test
      void whenSourceDoesNotExist_ShouldThrowIllegalArgumentException(
          final @TempDir File testFolder) {

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from.txt");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.copyFileToDirectory(from, new File(dir, "to.txt")))
            .withMessage("srcFile must be an existing file");
      }

      @Test
      void whenTargetIsFile_ShouldThrowIllegalArgumentException(final @TempDir File testFolder)
          throws IOException {

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from.txt");
        from.createNewFile();
        final File to = new File(dir, "to.txt");
        to.createNewFile();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.copyFileToDirectory(from, to))
            .withMessage("destDir cannot be an existing file");
      }

      @Test
      void whenTargetAlreadyExists_ShouldThrowFileAlreadyExistsException(
          final @TempDir File testFolder) throws IOException {

        final Charset encoding = StandardCharsets.UTF_8;

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from.txt");
        from.createNewFile();
        final File toDir = new File(dir, "to");
        toDir.mkdir();
        final File to = new File(toDir, "from.txt");
        to.createNewFile();

        Files.write(from.toPath(), "Whatever".getBytes(encoding));
        Files.write(to.toPath(), "Yihaa".getBytes(encoding));

        assertThatExceptionOfType(FileAlreadyExistsException.class)
            .isThrownBy(() -> FileUtils.copyFileToDirectory(from, toDir));

        assertThat(new String(Files.readAllBytes(to.toPath()), encoding)).isEqualTo("Yihaa");
      }
    }

    @Nested
    class Success {

      @Test
      void whenTargetDoesNotExist_ShouldCopyFileAndModifiedDate(final @TempDir File testFolder)
          throws IOException {

        final Charset encoding = StandardCharsets.UTF_8;
        final String test = "test";

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from.txt");
        from.createNewFile();
        final File toDir = new File(dir, "to");
        toDir.mkdir();

        Files.write(from.toPath(), test.getBytes(encoding));

        FileUtils.copyFileToDirectory(from, toDir);

        final File to = new File(toDir, "from.txt");
        assertThat(new String(Files.readAllBytes(to.toPath()), encoding)).isEqualTo(test);
        assertThat(from.lastModified()).isEqualTo(to.lastModified());
      }

      @Test
      void whenTargetHerarchyDoesNotExist_ShouldCopyFileAndModifiedDate(
          final @TempDir File testFolder) throws IOException {

        final Charset encoding = StandardCharsets.UTF_8;
        final String test = "test";

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from.txt");
        from.createNewFile();
        final File toDir = new File(new File(new File(new File(dir, "to"), "to"), "to"), "to");

        Files.write(from.toPath(), test.getBytes(encoding));

        FileUtils.copyFileToDirectory(from, toDir);

        final File to = new File(toDir, "from.txt");
        assertThat(new String(Files.readAllBytes(to.toPath()), encoding)).isEqualTo(test);
        assertThat(from.lastModified()).isEqualTo(to.lastModified());
      }

      @Test
      void whenTargetAlreadyExistsWithReplaceOption_ShouldCopyFileAndModifiedDate(
          final @TempDir File testFolder) throws IOException {

        final Charset encoding = StandardCharsets.UTF_8;

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from.txt");
        from.createNewFile();
        final File toDir = new File(dir, "to");
        toDir.mkdir();
        final File to = new File(toDir, "from.txt");
        to.createNewFile();

        Files.write(from.toPath(), "Whatever".getBytes(encoding));
        Files.write(to.toPath(), "Yihaa".getBytes(encoding));

        FileUtils.copyFileToDirectory(from, toDir, StandardCopyOption.REPLACE_EXISTING);

        assertThat(new String(Files.readAllBytes(to.toPath()), encoding)).isEqualTo("Whatever");
        assertThat(from.lastModified()).isEqualTo(to.lastModified());
      }
    }
  }

  @Nested
  class CopyDirectory {

    @Nested
    class Failure {

      @Test
      void whenSourceDoesNotExist_ShouldThrowIllegalArgumentException(
          final @TempDir File testFolder) {

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.copyDirectory(from, new File(dir, "to.txt")))
            .withMessage("srcDir must be an existing directory");
      }

      @Test
      void whenSourceIsFile_ShouldThrowIllegalArgumentException(final @TempDir File testFolder)
          throws IOException {

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from.txt");
        from.createNewFile();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.copyDirectory(from, new File(dir, "to")))
            .withMessage("srcDir must be an existing directory");
      }

      @Test
      void whenTargetAlreadyExists_ShouldThrowFileAlreadyExistsException(
          final @TempDir File testFolder) {

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from");
        from.mkdir();
        final File to = new File(dir, "to");
        to.mkdir();

        assertThatExceptionOfType(FileAlreadyExistsException.class)
            .isThrownBy(() -> FileUtils.copyDirectory(from, to));
      }

      @Test
      void whenTargetIsFile_ShouldThrowIllegalArgumentException(final @TempDir File testFolder)
          throws IOException {

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from");
        from.mkdir();
        final File to = new File(dir, "to.txt");
        to.createNewFile();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.copyDirectory(from, to))
            .withMessage("destDir cannot be an existing file");
      }

      @Test
      void whenTargetIsChildOfSource_ShouldThrowIllagalArgumentException(
          final @TempDir File testFolder) {

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from");
        from.mkdir();
        final File to = new File(from, "to");
        to.mkdir();
        final File to1 = new File(to, "to");

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

        final Charset encoding = StandardCharsets.UTF_8;

        final File from = new File(testFolder, "test");
        from.mkdir();
        final File from1 = new File(from, "from1.txt");
        from1.createNewFile();
        final File from2 = new File(from, "from2/from2.txt");
        from2.getParentFile().mkdirs();
        from2.createNewFile();
        final File from3 = new File(from, "from3/from3/from1.txt");
        from3.getParentFile().mkdirs();
        from3.createNewFile();
        final File toDir = new File(testFolder, "to");

        Files.write(from1.toPath(), "Whatever1".getBytes(encoding));
        Files.write(from2.toPath(), "Whatever2".getBytes(encoding));
        Files.write(from3.toPath(), "Whatever3".getBytes(encoding));

        FileUtils.copyDirectory(from, toDir);

        final File to1 = new File(toDir, "from1.txt");
        assertThat(new String(Files.readAllBytes(to1.toPath()), encoding)).isEqualTo("Whatever1");
        assertThat(from1.lastModified()).isEqualTo(to1.lastModified());
        final File to2 = new File(toDir, "from2/from2.txt");
        assertThat(new String(Files.readAllBytes(to2.toPath()), encoding)).isEqualTo("Whatever2");
        assertThat(from2.lastModified()).isEqualTo(to2.lastModified());
        final File to3 = new File(toDir, "from3/from3/from1.txt");
        assertThat(new String(Files.readAllBytes(to3.toPath()), encoding)).isEqualTo("Whatever3");
        assertThat(from3.lastModified()).isEqualTo(to3.lastModified());
      }

      @Test
      void whenTargetAlreadyExistsWithReplaceOption_ShouldCopyFilesAndModifiedDate(
          final @TempDir File testFolder) throws IOException {

        final Charset encoding = StandardCharsets.UTF_8;

        final File from = new File(testFolder, "test");
        from.mkdir();
        final File from1 = new File(from, "from1.txt");
        from1.createNewFile();
        final File from2 = new File(from, "from2/from2.txt");
        from2.getParentFile().mkdirs();
        from2.createNewFile();
        final File from3 = new File(from, "from3/from3/from1.txt");
        from3.getParentFile().mkdirs();
        from3.createNewFile();
        final File toDir = new File(testFolder, "to");
        toDir.mkdir();

        Files.write(from1.toPath(), "Whatever1".getBytes(encoding));
        Files.write(from2.toPath(), "Whatever2".getBytes(encoding));
        Files.write(from3.toPath(), "Whatever3".getBytes(encoding));

        FileUtils.copyDirectory(from, toDir, StandardCopyOption.REPLACE_EXISTING);

        final File to1 = new File(toDir, "from1.txt");
        assertThat(new String(Files.readAllBytes(to1.toPath()), encoding)).isEqualTo("Whatever1");
        assertThat(from1.lastModified()).isEqualTo(to1.lastModified());
        final File to2 = new File(toDir, "from2/from2.txt");
        assertThat(new String(Files.readAllBytes(to2.toPath()), encoding)).isEqualTo("Whatever2");
        assertThat(from2.lastModified()).isEqualTo(to2.lastModified());
        final File to3 = new File(toDir, "from3/from3/from1.txt");
        assertThat(new String(Files.readAllBytes(to3.toPath()), encoding)).isEqualTo("Whatever3");
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

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File file = new File(dir, "test.txt");
        file.createNewFile();

        try (FileChannel channel = new RandomAccessFile(file, "rw").getChannel()) {
          // Use the file channel to create a lock on the file.
          // This method blocks until it can retrieve the lock.
          final FileLock lock = channel.lock();

          // Call FileUtils.delete on the root directory. It should throw
          // an exception since we have a lock on the file.
          assertThatIOException().isThrownBy(() -> FileUtils.delete(dir));

          // Release the lock
          lock.release();
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

        final File root = new File(testFolder, "test");
        root.mkdir();

        File dir = new File(root, "test1");
        dir.mkdir();
        File file = new File(dir, "test1.txt");
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

      final File dir = new File(testFolder, "test");
      dir.mkdir();
      final File file = new File(dir, "test.txt");
      file.createNewFile();

      try (FileOutputStream outputStream = new FileOutputStream(file)) {
        outputStream.getChannel().lock();
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

  @Nested
  class ReadFileToString {

    @Nested
    class Failure {

      @Test
      void whenSourceIsDirectory_ShouldThrowIllegalArgumentException(
          final @TempDir File testFolder) {

        final File dir = new File(testFolder, "test");
        dir.mkdir();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.readFileToString(dir, StandardCharsets.UTF_8))
            .withMessage("srcFile must be an existing file");
      }

      @Test
      void whenSourceDoesNotExist_ShouldThrowIllegalArgumentException(
          final @TempDir File testFolder) {

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File from = new File(dir, "from.txt");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> FileUtils.readFileToString(from, StandardCharsets.UTF_8))
            .withMessage("srcFile must be an existing file");
      }
    }

    @Nested
    class Success {

      @Test
      void withFile_ShouldReturnFilecontentAsString(final @TempDir File testFolder)
          throws IOException {

        final Charset encoding = StandardCharsets.UTF_8;
        final String test = "ABDCEF\nGHIJKL  \nMNOPQRS\n\tTUVWXYZééé^ç^ç^ç^ç^pawewew";

        final File dir = new File(testFolder, "test");
        dir.mkdir();
        final File file = new File(dir, "file.txt");
        file.createNewFile();

        Files.write(file.toPath(), test.getBytes(encoding));

        assertThat(FileUtils.readFileToString(file, encoding)).isEqualTo(test);
      }
    }
  }
}
