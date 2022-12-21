/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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

package org.jodconverter.core;

import java.io.File;
import java.io.InputStream;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.job.ConversionJobWithOptionalSourceFormatUnspecified;

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
  @NonNull
  ConversionJobWithOptionalSourceFormatUnspecified convert(@NonNull File source);

  /**
   * Converts a source stream input stream.
   *
   * @param source The conversion input as an input stream.
   * @return The current conversion specification.
   */
  @NonNull
  ConversionJobWithOptionalSourceFormatUnspecified convert(@NonNull InputStream source);

  /**
   * Converts a source stream input stream.
   *
   * @param source The conversion input as an input stream.
   * @param closeStream Whether the {@link InputStream} is closed after the conversion terminates.
   * @return The current conversion specification.
   */
  @NonNull
  ConversionJobWithOptionalSourceFormatUnspecified convert(
      @NonNull InputStream source, boolean closeStream);

  /**
   * Gets all the {@link DocumentFormat} supported by the converter.
   *
   * @return A {@link DocumentFormatRegistry} containing the supported formats.
   */
  @NonNull
  DocumentFormatRegistry getFormatRegistry();
}
