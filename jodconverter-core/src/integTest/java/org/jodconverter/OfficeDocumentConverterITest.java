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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.star.document.UpdateDocMode;

import org.jodconverter.office.InstalledOfficeManagerHolder;
import org.jodconverter.office.OfficeException;

/**
 * Tests that the {@link OfficeDocumentConverter} class.
 *
 * @see OfficeDocumentConverter
 */
public class OfficeDocumentConverterITest extends BaseOfficeITest {

  private static final String OUTPUT_DIR =
      TEST_OUTPUT_DIR + OfficeDocumentConverterITest.class.getSimpleName() + "/";

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

    final File inputFile = new File(DOCUMENTS_DIR + "test.doc");
    final File outputFile = new File(OUTPUT_DIR + "convert_FromFileToFile.pdf");
    FileUtils.deleteQuietly(outputFile);

    final OfficeDocumentConverter officeDocumentConverter =
        new OfficeDocumentConverter(InstalledOfficeManagerHolder.getInstance());
    officeDocumentConverter.convert(inputFile, outputFile, null);

    assertTrue(outputFile.isFile() && outputFile.length() > 0);

    final Map<String, Object> loadProperties = new HashMap<>();
    loadProperties.put("Hidden", true);
    loadProperties.put("ReadOnly", true);
    loadProperties.put("Overwrite", true);
    loadProperties.put("UpdateDocMode", UpdateDocMode.QUIET_UPDATE);

    officeDocumentConverter.setDefaultLoadProperties(loadProperties);

    officeDocumentConverter.convert(
        inputFile,
        outputFile,
        officeDocumentConverter.getFormatRegistry().getFormatByExtension("pdf"),
        null);
    assertTrue(outputFile.isFile() && outputFile.length() > 0);
  }
}
