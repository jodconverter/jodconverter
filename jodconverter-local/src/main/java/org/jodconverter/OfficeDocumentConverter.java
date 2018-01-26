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

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;

/**
 * A OfficeDocumentConverter is responsible to execute the conversion of documents using an office
 * manager.
 *
 * <p>This class is maintained for backward compatibility, but {@link LocalConverter} should be used
 * instead.
 */
public class OfficeDocumentConverter {

  private final LocalConverter delegate;

  /**
   * Constructs a new instance of the class with the specified manager.
   *
   * @param officeManager The manager that will provide the office instance required for a
   *     conversion.
   */
  public OfficeDocumentConverter(final OfficeManager officeManager) {

    this(officeManager, DefaultDocumentFormatRegistry.getInstance());
  }

  /**
   * Constructs a new instance of the class with the specified manager and registry.
   *
   * @param officeManager The manager that will provide the office instance required for a
   *     conversion.
   * @param formatRegistry A collections of {@link DocumentFormat} supported by this converter.
   */
  public OfficeDocumentConverter(
      final OfficeManager officeManager, final DocumentFormatRegistry formatRegistry) {

    this.delegate =
        LocalConverter.builder()
            .officeManager(officeManager)
            .formatRegistry(formatRegistry)
            .build();
  }

  /**
   * Converts an input file to an output file. The files extensions are used to determine the input
   * and output {@link DocumentFormat}.
   *
   * @param inputFile The input file to convert.
   * @param outputFile The target output file.
   * @throws OfficeException If the conversion fails.
   */
  public void convert(final File inputFile, final File outputFile) throws OfficeException {

    convert(inputFile, outputFile, null);
  }

  /**
   * Converts an input file to an output file. The input file extension is used to determine the
   * input {@link DocumentFormat}.
   *
   * @param inputFile The input file to convert.
   * @param outputFile The target output file.
   * @param outputFormat The target output format.
   * @throws OfficeException If the conversion fails.
   */
  public void convert(
      final File inputFile, final File outputFile, final DocumentFormat outputFormat)
      throws OfficeException {

    convert(inputFile, outputFile, null, outputFormat);
  }

  /**
   * Converts an input file to an output file.
   *
   * @param inputFile The input file to convert.
   * @param outputFile The target output file.
   * @param inputFormat The source input format.
   * @param outputFormat The target output format.
   * @throws OfficeException If the conversion fails.
   */
  public void convert(
      final File inputFile,
      final File outputFile,
      final DocumentFormat inputFormat,
      final DocumentFormat outputFormat)
      throws OfficeException {

    convert(null, inputFile, outputFile, inputFormat, outputFormat);
  }

  /**
   * Converts an input file to an output file. The files extensions are used to determine the input
   * and output {@link DocumentFormat}.
   *
   * @param filterChain The FilterChain to be applied after the document is loaded and before it is
   *     stored (converted) in the new document format. A FilterChain is used to modify the document
   *     before the conversion. Filters are applied in the same order they appear in the chain.
   * @param inputFile The input file to convert.
   * @param outputFile The target output file.
   * @throws OfficeException If the conversion fails.
   */
  public void convert(final FilterChain filterChain, final File inputFile, final File outputFile)
      throws OfficeException {

    convert(filterChain, inputFile, outputFile, null);
  }

  /**
   * Converts an input file to an output file. The input file extension is used to determine the
   * input {@link DocumentFormat}.
   *
   * @param filterChain The FilterChain to be applied after the document is loaded and before it is
   *     stored (converted) in the new document format. A FilterChain is used to modify the document
   *     before the conversion. Filters are applied in the same order they appear in the chain.
   * @param inputFile The input file to convert.
   * @param outputFile The target output file.
   * @param outputFormat The target output format.
   * @throws OfficeException If the conversion fails.
   */
  public void convert(
      final FilterChain filterChain,
      final File inputFile,
      final File outputFile,
      final DocumentFormat outputFormat)
      throws OfficeException {

    convert(filterChain, inputFile, outputFile, null, outputFormat);
  }

  /**
   * Converts an input file to an output file.
   *
   * @param filterChain The FilterChain to be applied after the document is loaded and before it is
   *     stored (converted) in the new document format. A FilterChain is used to modify the document
   *     before the conversion. Filters are applied in the same order they appear in the chain.
   * @param inputFile The input file to convert.
   * @param outputFile The target output file.
   * @param inputFormat The source input format.
   * @param outputFormat The target output format.
   * @throws OfficeException If the conversion fails.
   */
  public void convert(
      final FilterChain filterChain,
      final File inputFile,
      final File outputFile,
      final DocumentFormat inputFormat,
      final DocumentFormat outputFormat)
      throws OfficeException {

    delegate.setFilterChain(filterChain);
    delegate
        .convert(inputFile)
        .as(
            inputFormat == null
                ? getFormatRegistry()
                    .getFormatByExtension(FilenameUtils.getExtension(inputFile.getName()))
                : inputFormat)
        .to(outputFile)
        .as(
            outputFormat == null
                ? getFormatRegistry()
                    .getFormatByExtension(FilenameUtils.getExtension(outputFile.getName()))
                : outputFormat)
        .execute();
  }

  /**
   * Gets all the {@link DocumentFormat} supported by the converter.
   *
   * @return A {@link DocumentFormatRegistry} containing the supported format.
   */
  public DocumentFormatRegistry getFormatRegistry() {

    return delegate.getFormatRegistry();
  }

  /**
   * Sets the default properties to use when we load (open) a document before a conversion,
   * regardless the input type of the document.
   *
   * @param defaultLoadProperties The default properties to apply when loading a document.
   */
  public void setDefaultLoadProperties(final Map<String, Object> defaultLoadProperties) {

    this.delegate.setLoadProperties(defaultLoadProperties);
  }
}
