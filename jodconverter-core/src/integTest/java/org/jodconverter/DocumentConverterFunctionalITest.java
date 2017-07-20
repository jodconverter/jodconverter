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

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.jodconverter.filter.RefreshFilter;
import org.jodconverter.office.OfficeException;

public class DocumentConverterFunctionalITest extends BaseOfficeITest {

  private static final String OUTPUT_DIR =
      TEST_OUTPUT_DIR + DocumentConverterFunctionalITest.class.getSimpleName() + "/";

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

  /**
   * Test the conversion of an HTML file that contains an image.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void htmlWithImageConversion() throws Exception {

    final File inputFile = new File(DOCUMENTS_DIR + "index.html");
    final File outputDir = new File(OUTPUT_DIR);

    // Convert the file to PDF
    convertFileToPdf(inputFile, outputDir, RefreshFilter.REFRESH);
  }

  /**
   * Test the conversion of an HTML file that contains an image.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void testHtmlConversion() throws Exception {

    final File inputFile = new File(DOCUMENTS_DIR + "test.html");
    final File outputDir = new File(OUTPUT_DIR);

    // Convert the file to PDF
    convertFileToPdf(inputFile, outputDir, RefreshFilter.REFRESH);
  }

  /**
   * Test the conversion of all the supported documents format.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void runAllPossibleConversions() throws Exception {

    final File dir = new File("src/integTest/resources/documents");
    final File[] files =
        dir.listFiles(
            new FilenameFilter() {
              public boolean accept(final File dir, final String name) {
                return name.charAt(0) != '.';
              }
            });

    for (final File inputFile : files) {
      // Convert the file to all supported formats
      convertFileToAllSupportedFormats(inputFile, null, RefreshFilter.REFRESH);
    }
  }
}
