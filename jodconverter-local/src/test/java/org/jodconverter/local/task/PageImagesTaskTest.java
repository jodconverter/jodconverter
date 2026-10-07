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

package org.jodconverter.local.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.jodconverter.local.ResourceUtil.documentFile;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.job.SourceDocumentSpecsFromFile;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.ImageFormat;
import org.jodconverter.local.office.LocalOfficeContext;

/** Contains tests for the {@link PageImagesTask} class that need no office. */
class PageImagesTaskTest {

  private static PageImagesTask task(final File directory, final String pages) {
    return new PageImagesTask(
        new SourceDocumentSpecsFromFile(documentFile("test.txt")),
        false,
        null,
        null,
        directory,
        "page",
        ImageFormat.PNG,
        0,
        0,
        90,
        pages,
        false);
  }

  @Test
  void whenTheDirectoryCannotBeCreated_ShouldThrowOfficeException(final @TempDir File testFolder)
      throws IOException {

    // A file where the directory should be.
    final var notADirectory = new File(testFolder, "images");
    Files.writeString(notADirectory.toPath(), "x");

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(() -> task(notADirectory, null).execute(mock(LocalOfficeContext.class)))
        .withMessage("Could not create the directory " + notADirectory);
  }

  @Test
  void withInvalidArguments_ShouldThrow(final @TempDir File testFolder) {

    final var source = new SourceDocumentSpecsFromFile(documentFile("test.txt"));

    assertThatNullPointerException()
        .isThrownBy(
            () ->
                new PageImagesTask(
                    source,
                    false,
                    null,
                    null,
                    null,
                    "page",
                    ImageFormat.PNG,
                    0,
                    0,
                    90,
                    null,
                    false));
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new PageImagesTask(
                    source,
                    false,
                    null,
                    null,
                    testFolder,
                    " ",
                    ImageFormat.PNG,
                    0,
                    0,
                    90,
                    null,
                    false));
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                new PageImagesTask(
                    source, false, null, null, testFolder, "page", null, 0, 0, 90, null, false));
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new PageImagesTask(
                    source,
                    false,
                    null,
                    null,
                    testFolder,
                    "page",
                    ImageFormat.PNG,
                    -1,
                    0,
                    90,
                    null,
                    false));
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new PageImagesTask(
                    source,
                    false,
                    null,
                    null,
                    testFolder,
                    "page",
                    ImageFormat.PNG,
                    0,
                    -1,
                    90,
                    null,
                    false));
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new PageImagesTask(
                    source,
                    false,
                    null,
                    null,
                    testFolder,
                    "page",
                    ImageFormat.PNG,
                    0,
                    0,
                    0,
                    null,
                    false));
    assertThatIllegalArgumentException().isThrownBy(() -> task(testFolder, "x"));
  }

  @Test
  void getImages_ShouldBeEmptyBeforeTheExport(final @TempDir File testFolder) {

    final var task = task(testFolder, "1-2");

    assertThat(task.getImages()).isEmpty();
    assertThat(task.toString())
        .contains("directory=" + testFolder, "format=png", "pages=[1, 2]", "hiddenSlides=false");
  }
}
