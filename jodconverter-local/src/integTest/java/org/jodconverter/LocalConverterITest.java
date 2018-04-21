/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

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
  public void convert_FromStreamToFileWithMissingInputFormat_ShouldThrowNullPointerException()
      throws Exception {

    final File outputFile =
        new File(testFolder.getRoot(), "convert_FromStreamToFileWithMissingInputFormat.pdf");
    FileUtils.deleteQuietly(outputFile);

    try (InputStream inputStream = new FileInputStream(SOURCE_FILE)) {
      LocalConverter.make().convert(inputStream).as(null).to(outputFile).execute();
    }
  }

  @Test
  public void convert_FromStreamToFileWithSupportedInputFormat_ShouldSucceeded() throws Exception {

    final File outputFile =
        new File(testFolder.getRoot(), "convert_FromStreamToFileWithSupportedInputFormat.pdf");
    FileUtils.deleteQuietly(outputFile);

    final InputStream inputStream = new FileInputStream(SOURCE_FILE);
    LocalConverter.make()
        .convert(inputStream)
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

    try (OutputStream outputStream = new FileOutputStream(outputFile)) {
      LocalConverter.make().convert(SOURCE_FILE).to(outputStream).as(null).execute();
    }
  }

  @Test
  public void convert_FromFileToStreamWithSupportedOutputFormat_ShouldSucceeded() throws Exception {

    final File outputFile =
        new File(testFolder.getRoot(), "convert_FromFileToStreamWithSupportedOutputFormat.pdf");
    FileUtils.deleteQuietly(outputFile);

    final OutputStream outputStream = new FileOutputStream(outputFile);
    LocalConverter.make()
        .convert(SOURCE_FILE)
        .to(outputStream)
        .as(DefaultDocumentFormatRegistry.getFormatByExtension("pdf"))
        .execute();

    assertThat(outputFile).isFile();
    assertThat(outputFile.length()).isGreaterThan(0L);
  }

  @Test
  public void convert_FromFileFileWithoutExtension_ShouldSucceeded() throws Exception {

    final File outputFile =
        new File(testFolder.getRoot(), "convert_FromFileFileWithoutExtension.pdf");
    FileUtils.deleteQuietly(outputFile);

    final OutputStream outputStream = new FileOutputStream(outputFile);
    LocalConverter.make()
        .convert(new File(DOCUMENTS_DIR + "test"))
        .to(outputStream)
        .as(DefaultDocumentFormatRegistry.getFormatByExtension("txt"))
        .execute();

    final String content = FileUtils.readFileToString(outputFile, Charset.forName("UTF-8"));
    assertThat(content).contains("Test document");
  }
}
