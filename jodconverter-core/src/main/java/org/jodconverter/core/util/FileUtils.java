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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Contains files helper functions. */
public final class FileUtils {

  private static final char UNIX_SEPARATOR = '/';
  private static final char WINDOWS_SEPARATOR = '\\';

  private static boolean endsWithSeparator(final @NonNull String filename) {

    if (filename.isEmpty()) {
      return false;
    }
    final var lastChar = filename.charAt(filename.length() - 1);
    return lastChar == UNIX_SEPARATOR || lastChar == WINDOWS_SEPARATOR;
  }

  /**
   * Copies a directory recursively, preserving the files last modified date.
   *
   * @param srcDir An existing directory to copy, must not be {@code null}.
   * @param destDir The target directory, must not be {@code null}.
   * @param options Options specifying how the copy should be done.
   * @throws IOException If an IO error occurs.
   */
  public static void copyDirectory(
      final @NonNull File srcDir,
      final @NonNull File destDir,
      final @Nullable CopyOption... options)
      throws IOException {
    Objects.requireNonNull(srcDir, "srcDir must not be null");
    Objects.requireNonNull(destDir, "destDir must not be null");

    final var srcPath = srcDir.toPath();
    final var destPath = destDir.toPath();

    AssertUtils.isTrue(Files.isDirectory(srcPath), "srcDir must be an existing directory");
    AssertUtils.isTrue(!Files.isRegularFile(destPath), "destDir cannot be an existing file");
    // Check that the destination is not a child of the source
    AssertUtils.isTrue(
        !destPath.toAbsolutePath().startsWith(srcPath.toAbsolutePath()),
        "destDir cannot be a child of srcDir");
    if (Files.isDirectory(destPath)) {
      if (Arrays.asList(options).contains(StandardCopyOption.REPLACE_EXISTING)) {
        delete(destDir);
      } else {
        throw new FileAlreadyExistsException(destDir.toString());
      }
    }

    Files.walkFileTree(srcPath, new CopyDir(srcPath, destPath, options));
  }

  /**
   * Deletes a file. If file is a directory, delete it and all subdirectories.
   *
   * @param file File or directory to delete, can be {@code null}.
   * @return {@code true} If the file or directory was deleted, {@code false} otherwise. The file or
   *     directory is considered deleted if it exists entering the function and if it does not exist
   *     when the function ends, meaning that a {@code null} input file or a file that does not
   *     exist will return {@code false}.
   * @throws IOException If an IO error occurs.
   */
  public static boolean delete(final @Nullable File file) throws IOException {
    if (file == null || !file.exists()) {
      return false;
    }

    final var pathToDelete = file.toPath();

    if (Files.isDirectory(pathToDelete)) {
      try {
        Files.walk(pathToDelete)
            .sorted(Comparator.reverseOrder())
            .forEach(
                path -> {
                  try {
                    Files.delete(path);
                  } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                  }
                });
      } catch (UncheckedIOException ex) { // NOPMD - Only cause is relevant
        throw ex.getCause();
      }
    } else {
      Files.delete(pathToDelete);
    }

    return !Files.exists(pathToDelete);
  }

  /**
   * Deletes a file, never throwing an exception. If file is a directory, delete it and all
   * subdirectories.
   *
   * @param file File or directory to delete, can be {@code null}.
   * @return {@code true} If the file or directory is deleted, {@code false} otherwise.
   */
  public static boolean deleteQuietly(final @Nullable File file) {
    try {
      return delete(file);
    } catch (IOException ignored) {
      return false;
    }
  }

  /**
   * Gets the name minus the path and extension from a full filename. The text after the last
   * forward or backslash and before the last dot is returned.
   *
   * @param filename The filename to query, may be {@code null}.
   * @return The name of the file without the path and extension, or an empty string if none exists.
   */
  public static @Nullable String getBaseName(final @Nullable String filename) {
    if (filename == null) {
      return null;
    }
    if (endsWithSeparator(filename)) {
      return "";
    }
    final var name = Path.of(filename).getFileName().toString();
    final var i = name.lastIndexOf('.');
    if (i == -1) {
      return name;
    }
    return name.substring(0, i);
  }

  /**
   * Gets the file extension from a full filename. The text after the last dot is returned.
   *
   * @param filename The filename to query, may be {@code null}.
   * @return The extension of the file, or an empty string if none exists.
   */
  public static @Nullable String getExtension(final @Nullable String filename) {
    if (filename == null) {
      return null;
    }
    if (endsWithSeparator(filename)) {
      return "";
    }
    final var name = Path.of(filename).getFileName().toString();
    final var i = name.lastIndexOf('.');
    if (i == -1) {
      return "";
    }
    return name.substring(i + 1);
  }

  /**
   * Gets the name minus the path from a full filename. The text after the last forward or backslash
   * is returned.
   *
   * @param filename The filename to query, may be {@code null}.
   * @return The name of the file without the path, or an empty string if none exists.
   */
  public static @Nullable String getName(final @Nullable String filename) {
    if (filename == null) {
      return null;
    }
    if (endsWithSeparator(filename)) {
      return "";
    }
    return Path.of(filename).getFileName().toString();
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private FileUtils() {
    throw new AssertionError("Utility class must not be instantiated");
  }

  /** Visitor that helps copy a directory recursively. */
  private static class CopyDir extends SimpleFileVisitor<Path> {
    private final Path sourceDir;
    private final Path targetDir;
    private final CopyOption[] options;

    /* default */ CopyDir(final Path sourceDir, final Path targetDir, final CopyOption... options) {
      super();

      this.sourceDir = sourceDir;
      this.targetDir = targetDir;
      this.options = options;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes)
        throws IOException {

      final var targetFile = targetDir.resolve(sourceDir.relativize(file));
      Files.copy(file, targetFile, options);
      targetFile.toFile().setLastModified(file.toFile().lastModified());

      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attributes)
        throws IOException {

      final var newDir = targetDir.resolve(sourceDir.relativize(dir));
      Files.createDirectory(newDir);

      return FileVisitResult.CONTINUE;
    }
  }
}
