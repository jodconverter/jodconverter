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

package org.jodconverter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.document.DefaultDocumentFormatRegistry;

public class LocalConverterITest extends AbstractOfficeITest {

  private static final File SOURCE_FILE = new File(DOCUMENTS_DIR + "test.doc");

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void convert_FromFileToFile_ShouldSucceeded() throws Exception {

    final File outputFile = new File(testFolder.getRoot(), "convert_FromFileToFile.pdf");
    FileUtils.deleteQuietly(outputFile);

    LocalConverter.make().convert(SOURCE_FILE).to(outputFile).execute();

    assertThat(outputFile).isFile();
    assertThat(outputFile.length()).isGreaterThan(0L);
  }

  @Test(expected = NullPointerException.class)
  public void convert_FromStreamToFileWithNullInputFormat_ShouldThrowNullPointerException()
      throws Exception {

    final File outputFile =
        new File(testFolder.getRoot(), "convert_FromStreamToFileWithMissingInputFormat.pdf");
    FileUtils.deleteQuietly(outputFile);

    try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
      LocalConverter.make().convert(stream).as(null).to(outputFile).execute();
    }
  }

  @Test
  public void convert_FromStreamToFileWithoutInputFormat_ShouldSucceeded() throws Exception {

    final File outputFile =
        new File(testFolder.getRoot(), "convert_FromStreamToFileWithoutInputFormat.pdf");
    FileUtils.deleteQuietly(outputFile);

    try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
      LocalConverter.make().convert(stream).to(outputFile).execute();
    }
  }

  @Test
  public void convert_FromStreamToFileWithSupportedInputFormat_ShouldSucceeded() throws Exception {

    final File outputFile =
        new File(testFolder.getRoot(), "convert_FromStreamToFileWithSupportedInputFormat.pdf");
    FileUtils.deleteQuietly(outputFile);

    final InputStream stream = Files.newInputStream(SOURCE_FILE.toPath());
    LocalConverter.make()
        .convert(stream)
        .as(DefaultDocumentFormatRegistry.getFormatByExtension("doc"))
        .to(outputFile)
        .execute();

    assertThat(outputFile).isFile();
    assertThat(outputFile.length()).isGreaterThan(0L);
  }

  @Test(expected = NullPointerException.class)
  public void convert_FromFileToStreamWithMissingOutputFormat_ShouldThrowNullPointerException()
      throws Exception {

    final File outputFile =
        new File(testFolder.getRoot(), "convert_FromFileToStreamWithMissingOutputFormat.pdf");
    FileUtils.deleteQuietly(outputFile);

    try (OutputStream stream = Files.newOutputStream(outputFile.toPath())) {
      LocalConverter.make().convert(SOURCE_FILE).to(stream).as(null).execute();
    }
  }

  @Test
  public void convert_FromFileToStreamWithSupportedOutputFormat_ShouldSucceeded() throws Exception {

    final File outputFile =
        new File(testFolder.getRoot(), "convert_FromFileToStreamWithSupportedOutputFormat.pdf");
    FileUtils.deleteQuietly(outputFile);

    final OutputStream stream = Files.newOutputStream(outputFile.toPath());
    LocalConverter.make()
        .convert(SOURCE_FILE)
        .to(stream)
        .as(DefaultDocumentFormatRegistry.getFormatByExtension("pdf"))
        .execute();

    assertThat(outputFile).isFile();
    assertThat(outputFile.length()).isGreaterThan(0L);
  }

  @Test
  public void convert_FromFileWithoutExtensionToFile_ShouldSucceeded() throws Exception {

    final File outputFile =
            new File(testFolder.getRoot(), "convert_FromFileWithoutExtensionToFile.pdf");
    FileUtils.deleteQuietly(outputFile);

    final OutputStream stream = Files.newOutputStream(outputFile.toPath());
    LocalConverter.make()
            .convert(new File(DOCUMENTS_DIR + "test"))
            .to(stream)
            .as(DefaultDocumentFormatRegistry.getFormatByExtension("txt"))
        .execute();

    assertThat(FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8))
        .contains("Test document");
  }
}
