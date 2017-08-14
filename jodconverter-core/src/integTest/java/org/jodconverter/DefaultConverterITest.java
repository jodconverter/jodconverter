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

package org.jodconverter;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.jodconverter.office.OfficeException;

public class DefaultConverterITest extends BaseOfficeITest {

  private static final File SOURCE_FILE = new File(DOCUMENTS_DIR + "test.doc");
  private static final String OUTPUT_DIR =
      TEST_OUTPUT_DIR + DefaultConverterITest.class.getSimpleName() + "/";

  /** Ensures we start with a fresh output directory. */
  @BeforeClass
  public static void createOutputDir() throws OfficeException {

    // Ensure we start with a fresh output directory
    final File outputDir = new File(OUTPUT_DIR);
    FileUtils.deleteQuietly(outputDir);
    outputDir.mkdirs();
  }

  /**  Deletes the output directory. */
  @AfterClass
  public static void deleteOutputDir() throws OfficeException {

    // Delete the output directory
    FileUtils.deleteQuietly(new File(OUTPUT_DIR));
  }

  @Test
  public void convert_FromFileToFile_ShouldSucceeded() throws Exception {

    final File outputFile = new File(OUTPUT_DIR + "convert_FromFileToFile.pdf");
    FileUtils.deleteQuietly(outputFile);

    converter.convert(SOURCE_FILE).to(outputFile).execute();

    assertTrue(outputFile.isFile() && outputFile.length() > 0);
  }

  @Test
  public void convert_UsingCustomStoreProperties_ShouldSucceeded() throws Exception {

    final File inputFile = new File(DOCUMENTS_DIR + "test_multi_page.doc");
    final File outputFile = new File(OUTPUT_DIR + "convert_FromMultipleFileToPDFOnlyPage2.pdf");
    FileUtils.deleteQuietly(outputFile);

    Map<String, Object> filterData = new HashMap<>();
    filterData.put("PageRange", "2");
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put("FilterData", filterData);
    converter.convert(inputFile).to(outputFile).storeWithProperties(customProperties).execute();

    assertTrue(outputFile.isFile() && outputFile.length() > 0);

    // TODO Check that only page 2 is printed (custom store properties are applied)
  }

  @Test(expected = NullPointerException.class)
  public void convert_FromStreamToFileWithMissingInputFormat_ShouldThrowNullPointerException()
      throws Exception {

    final File outputFile =
        new File(OUTPUT_DIR + "convert_FromStreamToFileWithMissingInputFormat.pdf");
    FileUtils.deleteQuietly(outputFile);

    try (InputStream inputStream = new FileInputStream(SOURCE_FILE)) {
      converter.convert(inputStream).as(null).to(outputFile).execute();
    }
  }

  @Test
  public void convert_FromStreamToFileWithSupportedInputFormat_ShouldSucceeded() throws Exception {

    final File outputFile =
        new File(OUTPUT_DIR + "convert_FromStreamToFileWithSupportedInputFormat.pdf");
    FileUtils.deleteQuietly(outputFile);

    final InputStream inputStream = new FileInputStream(SOURCE_FILE);
    converter
        .convert(inputStream)
        .as(formatRegistry.getFormatByExtension("doc"))
        .to(outputFile)
        .execute();

    assertTrue(outputFile.isFile() && outputFile.length() > 0);
  }

  @Test(expected = NullPointerException.class)
  public void convert_FromFileToStreamWithMissingOutputFormat_ShouldThrowNullPointerException()
      throws Exception {

    final File outputFile =
        new File(OUTPUT_DIR + "convert_FromFileToStreamWithMissingOutputFormat.pdf");
    FileUtils.deleteQuietly(outputFile);

    try (OutputStream outputStream = new FileOutputStream(outputFile)) {
      converter.convert(SOURCE_FILE).to(outputStream).as(null).execute();
    }
  }

  @Test
  public void convert_FromFileToStreamWithSupportedOutputFormat_ShouldSucceeded() throws Exception {

    final File outputFile =
        new File(OUTPUT_DIR + "convert_FromFileToStreamWithSupportedOutputFormat.pdf");
    FileUtils.deleteQuietly(outputFile);

    final OutputStream outputStream = new FileOutputStream(outputFile);
    converter
        .convert(SOURCE_FILE)
        .to(outputStream)
        .as(formatRegistry.getFormatByExtension("pdf"))
        .execute();

    assertTrue(outputFile.isFile() && outputFile.length() > 0);
  }
}
