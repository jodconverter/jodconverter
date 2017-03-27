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

package org.artofsolving.jodconverter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.filter.DefaultFilterChain;
import org.artofsolving.jodconverter.filter.FilterChain;
import org.artofsolving.jodconverter.filter.RefreshFilter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerBuilder;
import org.artofsolving.jodconverter.office.OfficeManager;

public class OfficeDocumentConverterFunctionalTest {

  private static final Logger logger =
      LoggerFactory.getLogger(OfficeDocumentConverterFunctionalTest.class);

  /**
   * Test the conversion of all the supported documents format.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void runAllPossibleConversions() throws Exception {

    final OfficeManager officeManager = new DefaultOfficeManagerBuilder().build();
    final OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
    final DocumentFormatRegistry formatRegistry = converter.getFormatRegistry();

    officeManager.start();
    try {
      final File dir = new File("src/test/resources/documents");
      final File[] files =
          dir.listFiles(
              new FilenameFilter() {
                public boolean accept(final File dir, final String name) {
                  return name.charAt(0) != '.';
                }
              });

      // Here we can reuse a unique FilterChain
      final FilterChain chain = new DefaultFilterChain(RefreshFilter.INSTANCE);

      for (final File inputFile : files) {
        final String inputExtension = FilenameUtils.getExtension(inputFile.getName());
        final DocumentFormat inputFormat = formatRegistry.getFormatByExtension(inputExtension);
        assertNotNull("unknown input format: " + inputExtension, inputFormat);
        final Set<DocumentFormat> outputFormats =
            formatRegistry.getOutputFormats(inputFormat.getInputFamily());
        for (final DocumentFormat outputFormat : outputFormats) {
          // LibreOffice 4 fails natively on those one
          if (inputFormat.getExtension().equals("odg")
              && outputFormat.getExtension().equals("svg")) {
            logger.info("-- skipping odg to svg test... ");
            continue;
          }
          if (outputFormat.getExtension().equals("sxc")) {
            logger.info("-- skipping * to sxc test... ");
            continue;
          }
          if (outputFormat.getExtension().equals("sxw")) {
            logger.info("-- skipping * to sxw test... ");
            continue;
          }
          if (outputFormat.getExtension().equals("sxi")) {
            logger.info("-- skipping * to sxi test... ");
            continue;
          }
          final File outputFile = File.createTempFile("test", "." + outputFormat.getExtension());
          outputFile.deleteOnExit();
          logger.info(
              "-- converting %s to %s... ",
              inputFormat.getExtension(), outputFormat.getExtension());
          converter.convert(chain, inputFile, outputFile, outputFormat);
          logger.info("done.\n");
          assertTrue(outputFile.isFile() && outputFile.length() > 0);
          //TODO use file detection to make sure outputFile is in the expected format
        }
      }
    } finally {
      officeManager.stop();
    }
  }
}
