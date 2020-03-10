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

package org.jodconverter.local;

import java.io.File;
import java.io.InputStream;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.job.ConversionJobWithOptionalSourceFormatUnspecified;

/**
 * Helper class that will create a {@link LocalConverter} using the previously installed {@link
 * org.jodconverter.core.office.OfficeManager} under the hood for each conversion.
 *
 * @see org.jodconverter.core.DocumentConverter
 * @see org.jodconverter.core.office.OfficeManager
 * @see org.jodconverter.core.office.InstalledOfficeManagerHolder
 */
public final class JodConverter { // NOPMD - Disable utility class name rule violation

  /**
   * Converts a source file that is stored on the local file system.
   *
   * @param source The conversion input as a file.
   * @return The current conversion specification.
   */
  @NonNull
  public static ConversionJobWithOptionalSourceFormatUnspecified convert(
      @NonNull final File source) {

    return LocalConverter.make().convert(source);
  }

  /**
   * Converts a source stream input stream.
   *
   * @param source The conversion input as an input stream.
   * @return The current conversion specification.
   */
  @NonNull
  public static ConversionJobWithOptionalSourceFormatUnspecified convert(
      @NonNull final InputStream source) {

    return LocalConverter.make().convert(source);
  }

  /**
   * Converts a source stream input stream.
   *
   * @param source The conversion input as an input stream.
   * @param closeStream Whether the {@link InputStream} is closed after the conversion terminates.
   * @return The current conversion specification.
   */
  @NonNull
  public static ConversionJobWithOptionalSourceFormatUnspecified convert(
      @NonNull final InputStream source, final boolean closeStream) {

    return LocalConverter.make().convert(source, closeStream);
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private JodConverter() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
