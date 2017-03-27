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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import com.sun.star.document.UpdateDocMode;

import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.filter.DefaultFilterChain;
import org.artofsolving.jodconverter.filter.FilterChain;
import org.artofsolving.jodconverter.filter.RefreshFilter;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;

/**
 * A OfficeDocumentConverter is responsible to execute the conversion of documents using an office
 * manager.
 */
public class OfficeDocumentConverter {

  private final OfficeManager officeManager;
  private final DocumentFormatRegistry formatRegistry;
  private Map<String, ?> defaultLoadProperties = createDefaultLoadProperties();

  /**
   * Constructs a new instance of the class with the specified manager.
   *
   * @param officeManager the manager that will provide the office instance required for a
   *     conversion.
   */
  public OfficeDocumentConverter(final OfficeManager officeManager) {

    this(officeManager, DefaultDocumentFormatRegistry.create());
  }

  /**
   * Constructs a new instance of the class with the specified manager and registry.
   *
   * @param officeManager the manager that will provide the office instance required for a
   *     conversion.
   * @param formatRegistry a collections of {@link DocumentFormat} supported by this converter.
   */
  public OfficeDocumentConverter(
      final OfficeManager officeManager, final DocumentFormatRegistry formatRegistry) {

    this.officeManager = officeManager;
    this.formatRegistry = formatRegistry;
  }

  /**
   * Converts an input file to an output file. The files extensions are used to determine the input
   * and output {@link DocumentFormat}.
   *
   * @param inputFile the input file to convert.
   * @param outputFile the target output file.
   * @throws OfficeException if the conversion fails.
   */
  public void convert(final File inputFile, final File outputFile) throws OfficeException {

    convert(
        inputFile,
        outputFile,
        formatRegistry.getFormatByExtension(FilenameUtils.getExtension(outputFile.getName())));
  }

  /**
   * Converts an input file to an output file. The input file extension is used to determine the
   * input {@link DocumentFormat}.
   *
   * @param inputFile the input file to convert.
   * @param outputFile the target output file.
   * @param outputFormat the target output format.
   * @throws OfficeException if the conversion fails.
   */
  public void convert(
      final File inputFile, final File outputFile, final DocumentFormat outputFormat)
      throws OfficeException {

    convert(
        inputFile,
        outputFile,
        formatRegistry.getFormatByExtension(FilenameUtils.getExtension(inputFile.getName())),
        outputFormat);
  }

  /**
   * Converts an input file to an output file.
   *
   * @param inputFile the input file to convert.
   * @param outputFile the target output file.
   * @param inputFormat the source input format.
   * @param outputFormat the target output format.
   * @throws OfficeException if the conversion fails.
   */
  public void convert(
      final File inputFile,
      final File outputFile,
      final DocumentFormat inputFormat,
      final DocumentFormat outputFormat)
      throws OfficeException {

    final DefaultConversionTask task =
        new DefaultConversionTask(inputFile, outputFile, inputFormat, outputFormat);
    task.setDefaultLoadProperties(defaultLoadProperties);
    task.setFilterChain(new DefaultFilterChain(RefreshFilter.INSTANCE));
    officeManager.execute(task);
  }

  /**
   * Converts an input file to an output file. The files extensions are used to determine the input
   * and output {@link DocumentFormat}.
   *
   * @param filterChain the FilterChain to be applied after the document is loaded and before it is
   *     stored (converted) in the new document format. A FilterChain is used to modify the document
   *     before the conversion. Filters are applied in the same order they appear in the chain.
   * @param inputFile the input file to convert.
   * @param outputFile the target output file.
   * @throws OfficeException if the conversion fails.
   */
  public void convert(final FilterChain filterChain, final File inputFile, final File outputFile)
      throws OfficeException {

    convert(
        filterChain,
        inputFile,
        outputFile,
        formatRegistry.getFormatByExtension(FilenameUtils.getExtension(outputFile.getName())));
  }

  /**
   * Converts an input file to an output file. The input file extension is used to determine the
   * input {@link DocumentFormat}.
   *
   * @param filterChain the FilterChain to be applied after the document is loaded and before it is
   *     stored (converted) in the new document format. A FilterChain is used to modify the document
   *     before the conversion. Filters are applied in the same order they appear in the chain.
   * @param inputFile the input file to convert.
   * @param outputFile the target output file.
   * @param outputFormat the target output format.
   * @throws OfficeException if the conversion fails.
   */
  public void convert(
      final FilterChain filterChain,
      final File inputFile,
      final File outputFile,
      final DocumentFormat outputFormat)
      throws OfficeException {

    convert(
        filterChain,
        inputFile,
        outputFile,
        formatRegistry.getFormatByExtension(FilenameUtils.getExtension(inputFile.getName())),
        outputFormat);
  }

  /**
   * Converts an input file to an output file.
   *
   * @param filterChain the FilterChain to be applied after the document is loaded and before it is
   *     stored (converted) in the new document format. A FilterChain is used to modify the document
   *     before the conversion. Filters are applied in the same order they appear in the chain.
   * @param inputFile the input file to convert.
   * @param outputFile the target output file.
   * @param inputFormat the source input format.
   * @param outputFormat the target output format.
   * @throws OfficeException if the conversion fails.
   */
  public void convert(
      final FilterChain filterChain,
      final File inputFile,
      final File outputFile,
      final DocumentFormat inputFormat,
      final DocumentFormat outputFormat)
      throws OfficeException {

    final DefaultConversionTask task =
        new DefaultConversionTask(inputFile, outputFile, inputFormat, outputFormat);
    task.setDefaultLoadProperties(defaultLoadProperties);
    task.setFilterChain(filterChain);
    officeManager.execute(task);
  }

  // Provides default properties to use when we load (open) a document before
  // a conversion, regardless the input type of the document.
  private Map<String, Object> createDefaultLoadProperties() {

    final Map<String, Object> loadProperties = new HashMap<>();
    loadProperties.put("Hidden", true);
    loadProperties.put("ReadOnly", true);
    loadProperties.put("UpdateDocMode", UpdateDocMode.QUIET_UPDATE);
    return loadProperties;
  }

  /** Gets the DocumentFormat supported by the converter. */
  public DocumentFormatRegistry getFormatRegistry() {

    return formatRegistry;
  }

  /**
   * Sets the default properties to use when we load (open) a document before a conversion,
   * regardless the input type of the document.
   */
  public void setDefaultLoadProperties(final Map<String, ?> defaultLoadProperties) {

    this.defaultLoadProperties = defaultLoadProperties;
  }
}
