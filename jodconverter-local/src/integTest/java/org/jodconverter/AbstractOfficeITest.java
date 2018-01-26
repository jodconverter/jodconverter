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
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.filter.DefaultFilterChain;
import org.jodconverter.filter.Filter;

public abstract class AbstractOfficeITest {

  @ClassRule public static TestRule managerResource = OfficeManagerResource.INSTANCE;

  protected static final String RESOURCES_DIR = "src/integTest/resources/";
  protected static final String DOCUMENTS_DIR = RESOURCES_DIR + "documents/";
  protected static final String TEST_OUTPUT_DIR = "build/integTest-results/";
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOfficeITest.class);

  void convertFileToAllSupportedFormats(
      final File sourceFile, final File outputDir, final Filter... filters) {

    // Detect input format
    final String inputExtension = FilenameUtils.getExtension(sourceFile.getName());
    final DocumentFormat inputFormat =
        DefaultDocumentFormatRegistry.getFormatByExtension(inputExtension);
    if (inputFormat == null) {
      LOGGER.info("-- skipping unsupported input format {}... ", inputExtension);
      return;
    }
    assertThat(inputFormat).as("check %s's input format", inputExtension).isNotNull();

    // Get all supported output formats
    final Set<DocumentFormat> outputFormats =
        DefaultDocumentFormatRegistry.getOutputFormats(inputFormat.getInputFamily());

    // Convert the input file into all the supported output formats.
    // This will create 1 output file per output format.
    for (final DocumentFormat outputFormat : outputFormats) {

      // Skip conversions that are not supported on all OS.
      if (StringUtils.equalsAny(
              inputFormat.getExtension(), "odg", "svg", "fodg", "fodp", "fods", "fodt")
          || StringUtils.equalsAny(
              outputFormat.getExtension(),
              "png",
              "sxc",
              "sxw",
              "sxi",
              "fodg",
              "fodp",
              "fods",
              "fodt")) {
        LOGGER.info(
            "-- skipping {} to {} test... ",
            inputFormat.getExtension(),
            outputFormat.getExtension());
        continue;
      }

      // Create the filter chain to use
      final DefaultFilterChain chain = new DefaultFilterChain(filters);

      // Create an output file
      final File targetFile =
          new File(outputDir, sourceFile.getName() + "." + outputFormat.getExtension());
      targetFile.deleteOnExit();

      // Delete existing file
      FileUtils.deleteQuietly(targetFile);

      // Convert the file to the desired format
      final ConvertRunner runner = new ConvertRunner(sourceFile, targetFile, chain);
      runner.run();
    }
  }
}
