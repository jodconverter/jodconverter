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
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link FileUtils} class. */
public class FileUtilsTest {

  @Test
  public void new_ClassWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(FileUtils.class);
  }

  @Test
  public void delete_WithNull_ShouldReturnFalse() throws IOException {
    assertThat(FileUtils.delete(null)).isFalse();
  }

  @Test
  public void delete_WithUnexistingFile_ShouldReturnNull() throws IOException {
    assertThat(FileUtils.delete(new File(UUID.randomUUID().toString()))).isFalse();
  }

  @Test
  public void delete_WithFolderWhenIOExceptionOccured_ShouldThrowIOException(
      final @TempDir File testFolder) throws IOException {

    // TODO: Find a way to make that test work on non-windows OS.
    assumeTrue(OSUtils.IS_OS_WINDOWS);

    final File dir = new File(testFolder, "test");
    dir.mkdir();
    final File file = new File(dir, "test.txt");
    file.createNewFile();

    try (FileChannel channel = new RandomAccessFile(file, "rw").getChannel()) {
      // Use the file channel to create a lock on the file.
      // This method blocks until it can retrieve the lock.
      FileLock lock = channel.lock();

      // Call FileUtils.delete on the root directory. It should throw
      // an exception since we have a lock on the file.
      assertThatIOException().isThrownBy(() -> FileUtils.delete(dir));

      // Release the lock
      lock.release();
    }
  }

  @Test
  public void delete_WithNotEmptyFolder_ShouldDeleteFolderRecursivelyAndReturnTrue(
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

  @Test
  public void deleteQuietly_WhenIOExceptionOccured_ShouldSwallowIOException(
      final @TempDir File testFolder) throws IOException {

    final File dir = new File(testFolder, "test");
    dir.mkdir();
    final File file = new File(dir, "test.txt");
    file.createNewFile();

    try (FileOutputStream outputStream = new FileOutputStream(file)) {
      outputStream.getChannel().lock();
      assertThatCode(() -> FileUtils.deleteQuietly(dir)).doesNotThrowAnyException();
    }
  }

  @Test
  public void getName_WithNull_ShouldReturnNull() {
    assertThat(FileUtils.getName(null)).isNull();
  }

  @Test
  public void getName_WithFullPath_ShouldReturnFileName() {
    assertThat(FileUtils.getName("a/b/c.txt")).isEqualTo("c.txt");
  }

  @Test
  public void getName_WithOnlyFileName_ShouldReturnFileName() {
    assertThat(FileUtils.getName("c.txt")).isEqualTo("c.txt");
  }

  @Test
  public void getName_WithFullPathWithoutExtension_ShouldReturnFileName() {
    assertThat(FileUtils.getName("a/b/c")).isEqualTo("c");
  }

  @Test
  public void getName_WithOnlyFileNameWithoutExtension_ShouldReturnFileName() {
    assertThat(FileUtils.getName("c")).isEqualTo("c");
  }

  @Test
  public void getName_WithFullDirectoryPath_ShouldReturnEmptyString() {
    assertThat(FileUtils.getName("a/b/")).isEqualTo("");
  }

  @Test
  public void getName_WithSlash_ShouldReturnEmptyString() {
    assertThat(FileUtils.getName("/")).isEqualTo("");
  }

  @Test
  public void getBaseName_WithNull_ShouldReturnNull() {
    assertThat(FileUtils.getBaseName(null)).isNull();
  }

  @Test
  public void getBaseName_WithFullPath_ShouldReturnBaseName() {
    assertThat(FileUtils.getBaseName("a/b/c.txt")).isEqualTo("c");
  }

  @Test
  public void getBaseName_WithOnlyFileName_ShouldReturnBaseName() {
    assertThat(FileUtils.getBaseName("c.txt")).isEqualTo("c");
  }

  @Test
  public void getBaseName_WithFullPathWithoutExtension_ShouldReturnFileName() {
    assertThat(FileUtils.getBaseName("a/b/c")).isEqualTo("c");
  }

  @Test
  public void getBaseName_WithOnlyFileNameWithoutExtension_ShouldReturnFileName() {
    assertThat(FileUtils.getBaseName("c")).isEqualTo("c");
  }

  @Test
  public void getBaseName_WithFullDirectoryPath_ShouldReturnEmptyString() {
    assertThat(FileUtils.getBaseName("a/b/")).isEqualTo("");
  }

  @Test
  public void getBaseName_WithSlash_ShouldReturnEmptyString() {
    assertThat(FileUtils.getBaseName("/")).isEqualTo("");
  }

  @Test
  public void getExtension_WithNull_ShouldReturnNull() {
    assertThat(FileUtils.getExtension(null)).isNull();
  }

  @Test
  public void getExtension_WithFullPath_ShouldReturnExtension() {
    assertThat(FileUtils.getExtension("a/b/c.txt")).isEqualTo("txt");
  }

  @Test
  public void getExtension_WithOnlyFileName_ShouldReturnExtension() {
    assertThat(FileUtils.getExtension("c.txt")).isEqualTo("txt");
  }

  @Test
  public void getExtension_WithFullPathWithoutExtension_ShouldReturnEmptyString() {
    assertThat(FileUtils.getExtension("a/b/c")).isEqualTo("");
  }

  @Test
  public void getExtension_WithOnlyFileNameWithoutExtension_ShouldReturnEmptyString() {
    assertThat(FileUtils.getExtension("c")).isEqualTo("");
  }

  @Test
  public void getExtension_WithFullDirectoryPath_ShouldReturnEmptyString() {
    assertThat(FileUtils.getExtension("a/b/")).isEqualTo("");
  }

  @Test
  public void getExtension_WithSlash_ShouldReturnEmptyString() {
    assertThat(FileUtils.getExtension("/")).isEqualTo("");
  }

  @Test
  public void getExtension_WithEmptyString_ShouldReturnEmptyString() {
    assertThat(FileUtils.getExtension("")).isEqualTo("");
  }

  @Test
  public void getExtension_WithDot_ShouldReturnEmptyString() {
    assertThat(FileUtils.getExtension(".")).isEqualTo("");
  }

  @Test
  public void getExtension_WithDotButNoExtension_ShouldReturnEmptyString() {
    assertThat(FileUtils.getExtension("/test/a.")).isEqualTo("");
  }

  @Test
  public void getExtension_WithDotButNoBaseNameNorExtension_ShouldReturnEmptyString() {
    assertThat(FileUtils.getExtension("/test/.")).isEqualTo("");
  }

  @Test
  public void getFullPath_WithNull_ShouldReturnNull() {
    assertThat(FileUtils.getBaseName(null)).isNull();
  }
}
