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

import java.io.File;
import java.io.InputStream;

import org.jodconverter.document.DocumentFormat;
import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.job.ConversionJobWithSourceSpecified;
import org.jodconverter.job.SourceDocumentSpecs;

/**
 * A DocumentConverter is responsible to execute the conversion of documents using an office
 * manager.
 */
public interface DocumentConverter {

  /**
   * Converts a source file that is stored on the local file system.
   *
   * @param source The conversion input as a file.
   * @return The current conversion specification.
   */
  ConversionJobWithSourceSpecified convert(File source);

  /**
   * Converts a source file that is stored on the local file system.
   *
   * @param source The conversion input as a file.
   * @param format The format of the document.
   * @return The current conversion specification.
   */
  ConversionJobWithSourceSpecified convert(File source, DocumentFormat format);

  /**
   * Converts a source stream input stream.
   *
   * @param source The conversion input as an input stream.
   * @param format The format of the document.
   * @return The current conversion specification.
   */
  ConversionJobWithSourceSpecified convert(InputStream source, DocumentFormat format);

  /**
   * Converts a source stream input stream.
   *
   * @param source The conversion input as an input stream.
   * @param format The format of the document.
   * @param close {@code true} to close the input stream once consume, {@code false} otherwise.
   * @return The current conversion specification.
   */
  ConversionJobWithSourceSpecified convert(
      InputStream source, DocumentFormat format, boolean close);

  /**
   * Converts a source document using the given specifications.
   *
   * @param source The conversion input as a document specifications.
   * @return The current conversion specification.
   */
  ConversionJobWithSourceSpecified convert(SourceDocumentSpecs source);

  /**
   * Gets all the DocumentFormat supported by the converter.
   *
   * @return A {@link DocumentFormatRegistry} containing the supported format.
   */
  DocumentFormatRegistry getFormatRegistry();
}
