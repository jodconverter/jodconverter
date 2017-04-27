/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.jodconverter.filter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.BaseOfficeTest;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;

public abstract class FilterTest extends BaseOfficeTest {

  private static final Logger logger = LoggerFactory.getLogger(FilterTest.class);

  protected void testFilters(
      final File sourceFile,
      final File outputDir,
      final String outputFilePrefix,
      final Filter... filters)
      throws OfficeException {

    // Create the filter chain to use
    final DefaultFilterChain chain = new DefaultFilterChain(filters);

    // Detect input format
    final String inputExtension = FilenameUtils.getExtension(sourceFile.getName());
    final DocumentFormat inputFormat = formatRegistry.getFormatByExtension(inputExtension);
    assertNotNull("Unknown input format: " + inputExtension, inputFormat);

    // Get the output formats supported for this input format
    final Set<DocumentFormat> outputFormats =
        formatRegistry.getOutputFormats(inputFormat.getInputFamily());

    // Convert the input file into all the supported output formats.
    // This will create 1 output file per output format.
    for (final DocumentFormat outputFormat : outputFormats) {

      // Skip unsupported conversion
      if (StringUtils.equalsAny(outputFormat.getExtension(), "sxc", "sxw", "sxi")) {
        logger.info("-- skipping {} to {} test... ", inputExtension, outputFormat.getExtension());
        continue;
      }

      // Create an output file
      final File outputFile =
          new File(outputDir, outputFilePrefix + "." + outputFormat.getExtension());

      // Apply the conversion
      logger.info(
          "-- converting {} to {}... ", inputFormat.getExtension(), outputFormat.getExtension());
      converter.convert(chain, sourceFile, outputFile, outputFormat);
      logger.info("done.\n");

      // Check that the created file is not empty. The programmer still have to
      // manually if the content of the output file looks good.
      assertTrue(outputFile.isFile() && outputFile.length() > 0);

      // Reset the chain in order to reuse it.
      chain.reset();
    }
  }
}
