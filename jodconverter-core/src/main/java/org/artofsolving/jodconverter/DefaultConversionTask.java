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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lang.XComponent;

import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.filter.FilterChain;
import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;

/** Represents the default behavior for a conversion task. */
public class DefaultConversionTask extends AbstractConversionTask {

  private static final Logger logger = LoggerFactory.getLogger(DefaultConversionTask.class);

  private final DocumentFormat inputFormat;
  private final DocumentFormat outputFormat;
  private Map<String, ?> defaultLoadProperties;
  private FilterChain filterChain;

  /**
   * Creates a new conversion task from a specified source to a specified target.
   *
   * @param inputFile source file.
   * @param outputFile target file.
   * @param inputFormat input format.
   * @param outputFormat output format.
   */
  public DefaultConversionTask(
      final File inputFile,
      final File outputFile,
      final DocumentFormat inputFormat,
      final DocumentFormat outputFormat) {
    super(inputFile, outputFile);

    this.inputFormat = inputFormat;
    this.outputFormat = outputFormat;
  }

  @Override
  public void execute(final OfficeContext context) throws OfficeException {

    logger.info("Executing default conversion task...");
    super.execute(context);
  }

  /**
   * Gets the default properties to be applied when the input document is loaded.
   *
   * @return the default properties to be applied when loading the document.
   */
  public Map<String, ?> getDefaultLoadProperties() {

    return defaultLoadProperties;
  }

  /**
   * Gets the filter chain to be applied when modifying the document.
   *
   * @return filterChain to use with this task.
   */
  public FilterChain getFilterChain() {

    return filterChain;
  }

  /**
   * Gets the input document format to convert.
   *
   * @return the input DocumentFormat.
   */
  public DocumentFormat getInputFormat() {
    return inputFormat;
  }

  @Override
  protected Map<String, ?> getLoadProperties() throws OfficeException {

    final Map<String, Object> loadProperties = new HashMap<>();
    if (defaultLoadProperties != null) {
      loadProperties.putAll(defaultLoadProperties);
    }
    if (inputFormat != null && inputFormat.getLoadProperties() != null) {
      loadProperties.putAll(inputFormat.getLoadProperties());
    }
    return loadProperties;
  }

  /**
   * Gets the output document format to convert.
   *
   * @return the output DocumentFormat.
   */
  public DocumentFormat getOutputFormat() {
    return outputFormat;
  }

  @Override
  protected Map<String, ?> getStoreProperties(final XComponent document) throws OfficeException {

    return outputFormat.getStoreProperties(OfficeDocumentUtils.getDocumentFamily(document));
  }

  // Don't allow override
  @Override
  protected final void modifyDocument(final OfficeContext context, final XComponent document)
      throws OfficeException {

    if (filterChain != null) {
      filterChain.doFilter(context, document);
    }
  }

  /**
   * Sets the default properties to be applied when the input document is loaded.
   *
   * @param defaultLoadProperties the default properties to ne applied when loading the document.
   */
  public void setDefaultLoadProperties(final Map<String, ?> defaultLoadProperties) {

    this.defaultLoadProperties = defaultLoadProperties;
  }

  /**
   * Sets the filter chain to be applied when modifying the document.
   *
   * @param filterChain filterChain to use with this task.
   */
  public void setFilterChain(final FilterChain filterChain) {

    this.filterChain = filterChain;
  }
}
