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

package org.artofsolving.jodconverter.filter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerBuilder;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;

public class GraphicInserterFilterTest {

  private static final String SOURCE_FILE = "src/test/resources/documents/test.doc";
  private static final String SOURCE_MULTI_PAGE_FILE =
      "src/test/resources/documents/test_multi_page.doc";
  private static final String IMAGE_FILE = "src/test/resources/images/sample-1.jpg";
  private static final String OUTPUT_DIR =
      "test-output/" + GraphicInserterFilterTest.class.getSimpleName();

  private static final Logger logger = LoggerFactory.getLogger(GraphicInserterFilterTest.class);

  private static OfficeManager officeManager;
  private static OfficeDocumentConverter converter;
  private static DocumentFormatRegistry formatRegistry;

  /**
   * Starts a default office manager and clears the output directory before the execution if the
   * first test in this class.
   *
   * @throws Exception if an error occurs.
   */
  @BeforeClass
  public static void setUpBeforeClass() throws OfficeException {

    final File testOutputDir = new File(OUTPUT_DIR);
    FileUtils.deleteQuietly(testOutputDir);

    // Start an office manager
    officeManager = new DefaultOfficeManagerBuilder().build();
    converter = new OfficeDocumentConverter(officeManager);
    formatRegistry = converter.getFormatRegistry();

    officeManager.start();
  }

  /**
   * Stops the office manager started in the setUpBeforeClass method.
   *
   * @throws Exception if an error occurs.
   */
  @AfterClass
  public static void tearDownAfterClass() throws OfficeException {

    officeManager.stop();
  }

  /**
   * Test the conversion of a document inserting a graphic along the way.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_WithCustomizedProperties() throws OfficeException {

    final File sourceFile = new File(SOURCE_MULTI_PAGE_FILE);
    final File sourceImage = new File(IMAGE_FILE);
    final File testOutputDir = new File(OUTPUT_DIR);

    // Create the properties of the filter
    Map<String, Object> props =
        GraphicInserterFilter.createDefaultProperties(
            50, // Horizontal Position, 5 CM
            100 // Vertical Position, 10 CM
            );

    // Add a special property to add the image on the second page
    props.put("AnchorPageNo", (short) 2);

    // Create the GraphicInserterFilter to test.
    GraphicInserterFilter giFilter = new GraphicInserterFilter(sourceImage.getPath(), props);

    // Create the filter chain to use
    final DefaultFilterChain chain = new DefaultFilterChain(giFilter, RefreshFilter.INSTANCE);

    final String inputExtension = FilenameUtils.getExtension(sourceFile.getName());
    final DocumentFormat inputFormat = formatRegistry.getFormatByExtension(inputExtension);
    assertNotNull("unknown input format: " + inputExtension, inputFormat);
    final Set<DocumentFormat> outputFormats =
        formatRegistry.getOutputFormats(inputFormat.getInputFamily());
    for (final DocumentFormat outputFormat : outputFormats) {
      // Skip unsupported conversion
      if (StringUtils.equalsAny(outputFormat.getExtension(), "sxc", "sxw", "sxi")) {
        logger.info("-- skipping {} to {} test... ", inputExtension, outputFormat.getExtension());
        continue;
      }

      // Create an output file
      final File outputFile =
          new File(testOutputDir, "test.secondpage." + outputFormat.getExtension());

      // Apply the conversion
      logger.info(
          "-- converting {} to {}... ", inputFormat.getExtension(), outputFormat.getExtension());
      converter.convert(chain, sourceFile, outputFile, outputFormat);
      logger.info("done.\n");
      assertTrue(outputFile.isFile() && outputFile.length() > 0);

      // Reset the chain in order to reuse it.
      chain.reset();
    }
  }

  /**
   * Test the conversion of a document inserting a graphic along the way.
   *
   * @throws OfficeException if an error occurs.
   */
  @Test
  public void doFilter_WithDefaultProperties() throws OfficeException {

    final File sourceFile = new File(SOURCE_FILE);
    final File sourceImage = new File(IMAGE_FILE);
    final File testOutputDir = new File(OUTPUT_DIR);

    // Create the GraphicInserterFilter to test.
    GraphicInserterFilter giFilter =
        new GraphicInserterFilter(
            sourceImage.getPath(),
            50, // Horizontal Position // 5 CM
            100); // Vertical Position // 10 CM

    // Create the filter chain to use
    final DefaultFilterChain chain = new DefaultFilterChain(giFilter, RefreshFilter.INSTANCE);

    final String inputExtension = FilenameUtils.getExtension(sourceFile.getName());
    final DocumentFormat inputFormat = formatRegistry.getFormatByExtension(inputExtension);
    assertNotNull("unknown input format: " + inputExtension, inputFormat);
    final Set<DocumentFormat> outputFormats =
        formatRegistry.getOutputFormats(inputFormat.getInputFamily());
    for (final DocumentFormat outputFormat : outputFormats) {
      // Skip unsupported conversion
      if (StringUtils.equalsAny(outputFormat.getExtension(), "sxc", "sxw", "sxi")) {
        logger.info("-- skipping {} to {} test... ", inputExtension, outputFormat.getExtension());
        continue;
      }

      // Create an output file
      final File outputFile =
          new File(testOutputDir, "test.original." + outputFormat.getExtension());

      // Apply the conversion
      logger.info(
          "-- converting {} to {}... ", inputFormat.getExtension(), outputFormat.getExtension());
      converter.convert(chain, sourceFile, outputFile, outputFormat);
      logger.info("done.\n");
      assertTrue(outputFile.isFile() && outputFile.length() > 0);

      // Reset the chain in order to reuse it.
      chain.reset();
    }
  }

  /**
   * Test the conversion of a document inserting a graphic along the way. The image will be resize
   * (smaller).
   *
   * @throws OfficeException if an error occurs.
   */
  @Test
  public void doFilter_WithDefaultPropertiesAndSmallerSize() throws OfficeException {

    final File sourceFile = new File(SOURCE_FILE);
    final File sourceImage = new File(IMAGE_FILE);
    final File testOutputDir = new File(OUTPUT_DIR);

    // Create the GraphicInserterFilter to test.
    GraphicInserterFilter giFilter =
        new GraphicInserterFilter(
            sourceImage.getPath(),
            74, // Image Width // 7.4 CM (half the original size)
            56, // Image Height // 5.6 CM (half the original size)
            30, // Horizontal Position // 3 CM
            50); // Vertical Position // 5 CM

    // Create the filter chain to use
    final DefaultFilterChain chain = new DefaultFilterChain(giFilter, RefreshFilter.INSTANCE);

    final String inputExtension = FilenameUtils.getExtension(sourceFile.getName());
    final DocumentFormat inputFormat = formatRegistry.getFormatByExtension(inputExtension);
    assertNotNull("unknown input format: " + inputExtension, inputFormat);
    final Set<DocumentFormat> outputFormats =
        formatRegistry.getOutputFormats(inputFormat.getInputFamily());
    for (final DocumentFormat outputFormat : outputFormats) {
      // Skip unsupported conversion
      if (StringUtils.equalsAny(outputFormat.getExtension(), "sxc", "sxw", "sxi")) {
        logger.info("-- skipping {} to {} test... ", inputExtension, outputFormat.getExtension());
        continue;
      }

      // Create an output file
      final File outputFile =
          new File(testOutputDir, "test.smaller." + outputFormat.getExtension());

      // Apply the conversion
      logger.info(
          "-- converting {} to {}... ", inputFormat.getExtension(), outputFormat.getExtension());
      converter.convert(chain, sourceFile, outputFile, outputFormat);
      logger.info("done.\n");
      assertTrue(outputFile.isFile() && outputFile.length() > 0);

      // Reset the chain in order to reuse it.
      chain.reset();
    }
  }
}
