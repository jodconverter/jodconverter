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
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOfficeITest.class);

  protected void convertFileToAllSupportedFormats(
      final File sourceFile, final File outputDir, final Filter... filters) {

    // Detect input format
    final String inputExt = FilenameUtils.getExtension(sourceFile.getName());
    final DocumentFormat inputFormat = DefaultDocumentFormatRegistry.getFormatByExtension(inputExt);
    if (inputFormat == null) {
      LOGGER.info("Skipping unsupported input format {}", inputExt);
      return;
    }
    assertThat(inputFormat).as("check %s's input format", inputExt).isNotNull();

    // Get all supported output formats
    final Set<DocumentFormat> outputFormats =
        DefaultDocumentFormatRegistry.getOutputFormats(inputFormat.getInputFamily());

    // Convert the input file into all the supported output formats.
    // This will create 1 output file per output format.
    for (final DocumentFormat outputFormat : outputFormats) {

      if (SystemUtils.IS_OS_WINDOWS) {
        if (StringUtils.equalsAny(outputFormat.getExtension(), "sxc", "sxi", "sxw")) {
          LOGGER.info(
              "Skipping {} to {} test", inputFormat.getExtension(), outputFormat.getExtension());
          continue;
        }
      } else if (SystemUtils.IS_OS_FREE_BSD) {
        if (StringUtils.equalsAny(outputFormat.getExtension(), "sxw")) {
          LOGGER.info(
              "Skipping {} to {} test", inputFormat.getExtension(), outputFormat.getExtension());
          continue;
        }
      } else if (SystemUtils.IS_OS_MAC) {
        if (StringUtils.equalsAny(outputFormat.getExtension(), "sxw")) {
          LOGGER.info(
              "Skipping {} to {} test", inputFormat.getExtension(), outputFormat.getExtension());
          continue;
        }
      } else if (SystemUtils.IS_OS_UNIX) {
        if (StringUtils.equalsAny(outputFormat.getExtension(), "sxw")) {
          LOGGER.info(
              "Skipping {} to {} test", inputFormat.getExtension(), outputFormat.getExtension());
          continue;
        }
      }

      //      // Skip conversions that are not supported on all OS.
      //      if (StringUtils.equalsAny(
      //              inputFormat.getExtension(), "odg", "svg", "fodg", "fodp", "fods", "fodt")
      //          || StringUtils.equalsAny(
      //              outputFormat.getExtension(),
      //              // Not supported by all office installations
      //              "png",
      //              "jpg",
      //              "jpeg",
      //              "tif",
      //              "tiff",
      //              "gif",
      //              "sxc",
      //              "sxw",
      //              "sxi",
      //              "fodg",
      //              "fodp",
      //              "fods",
      //              "fodt",
      //              // AOO Cannot save following extension.
      //              // See https://forum.openoffice.org/en/forum/viewtopic.php?f=15&t=92508
      //              "docx",
      //              "xlsx",
      //              "pptx")) {
      //        LOGGER.info(
      //            "Skipping {} to {} test", inputFormat.getExtension(),
      // outputFormat.getExtension());
      //        continue;
      //      }

      // Create the filter chain to use
      final DefaultFilterChain chain = new DefaultFilterChain(filters);

      // Create an output file
      final File targetFile =
          new File(outputDir, sourceFile.getName() + "." + outputFormat.getExtension());
      targetFile.deleteOnExit();

      // Delete existing file
      FileUtils.deleteQuietly(targetFile);

      // Convert the file to the desired format
      try {
        new ConvertRunner(sourceFile, targetFile, chain).run();
      } catch (Exception ignore) {
        // Ignore.
      }
    }
  }
}
