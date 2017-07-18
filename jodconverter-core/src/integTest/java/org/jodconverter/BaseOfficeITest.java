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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.document.DocumentFormat;
import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.filter.DefaultFilterChain;
import org.jodconverter.filter.Filter;
import org.jodconverter.office.OfficeException;

public abstract class BaseOfficeITest {

  @ClassRule public static TestRule officeManagerResource = OfficeManagerResource.INSTANCE;

  protected static DefaultConverter converter;
  protected static DocumentFormatRegistry formatRegistry;
  protected static final String RESOURCES_DIR = "src/integTest/resources/";
  protected static final String DOCUMENTS_DIR = RESOURCES_DIR + "documents/";
  protected static final String TEST_OUTPUT_DIR = "test-output/";
  private static final Logger logger = LoggerFactory.getLogger(BaseOfficeITest.class);

  /**
   * Starts a default office manager before the execution of the first test in this class.
   *
   * @throws OfficeException if an error occurs.
   */
  @BeforeClass
  public static void createConverter() throws OfficeException {

    // Start an office manager
    converter = DefaultConverter.make();
    formatRegistry = converter.getFormatRegistry();
  }

  protected void convertFileToAllSupportedFormats(
      final File inputFile, final File outputDir, final Filter... filters) throws Exception {

    convertFileToAllSupportedFormats(inputFile, outputDir, null, filters);
  }

  protected void convertFileToAllSupportedFormats(
      final File inputFile,
      final File outputDir,
      final String outputFilePrefix,
      final Filter... filters)
      throws Exception {

    // Create the filter chain to use
    final DefaultFilterChain chain = new DefaultFilterChain(filters);

    // Detect input format
    // Detect input format
    final String inputExtension = FilenameUtils.getExtension(inputFile.getName());
    final DocumentFormat inputFormat = formatRegistry.getFormatByExtension(inputExtension);
    if (inputFormat == null) {
      logger.info("-- skipping unsupported input format {}... ", inputExtension);
      return;
    }
    assertNotNull("unknown input format: " + inputExtension, inputFormat);

    // Get all supported output formats
    final Set<DocumentFormat> outputFormats =
        formatRegistry.getOutputFormats(inputFormat.getInputFamily());

    // Convert the input file into all the supported output formats.
    // This will create 1 output file per output format.
    for (final DocumentFormat outputFormat : outputFormats) {

      // Skip conversions that are not supported on all OS.
      if (inputFormat.getExtension().equals("odg") && outputFormat.getExtension().equals("svg")) {
        logger.info("-- skipping odg to svg test... ");
        continue;
      }
      if (StringUtils.equalsAny(outputFormat.getExtension(), "png", "sxc", "sxw", "sxi")) {
        logger.info("-- skipping {} to {} test... ", inputExtension, outputFormat.getExtension());
        continue;
      }

      // Create an output file
      File outputFile = null;
      if (outputDir == null) {
        outputFile =
            File.createTempFile(
                outputFilePrefix == null ? "test" : outputFilePrefix,
                "." + outputFormat.getExtension());
        outputFile.deleteOnExit();
      } else {
        outputFile =
            new File(
                outputDir,
                (outputFilePrefix == null
                        ? FilenameUtils.getBaseName(inputFile.getName())
                        : outputFilePrefix)
                    + "."
                    + outputFormat.getExtension());

        // Delete existing file
        FileUtils.deleteQuietly(outputFile);
      }

      // Convert the file
      logger.info(
          "-- converting {} to {}... ", inputFormat.getExtension(), outputFormat.getExtension());
      try {
        converter
            .convert(inputFile, inputFormat)
            .to(outputFile, outputFormat)
            .modifyWith(chain)
            .execute();

        logger.info("done.\n");

        // Check that the created file is not empty. The programmer still have to
        // manually if the content of the output file looks good.
        assertTrue(outputFile.isFile() && outputFile.length() > 0);

        //TODO use file detection to make sure outputFile is in the expected format

        // Reset the chain in order to reuse it.
        chain.reset();

      } catch (OfficeException ex) {
        // Log the error.
        String message =
            "Unable to convert from "
                + inputFormat.getExtension()
                + " to "
                + outputFormat.getExtension()
                + ".";
        if (ex.getCause() instanceof com.sun.star.task.ErrorCodeIOException) {
          com.sun.star.task.ErrorCodeIOException ioEx =
              (com.sun.star.task.ErrorCodeIOException) ex.getCause();
          logger.error(message + " " + ioEx.getMessage(), ioEx);
        } else {
          logger.error(message + " " + ex.getMessage(), ex);
        }

        throw ex;
      }
    }
  }
}
