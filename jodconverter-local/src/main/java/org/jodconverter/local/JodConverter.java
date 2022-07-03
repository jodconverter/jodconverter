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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.job.ConversionJobWithOptionalSourceFormatUnspecified;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.filter.text.LinkedImagesHtmlEncodingFilter;
import org.jodconverter.local.office.LocalOfficeManager;

/**
 * Helper class that will create a {@link LocalConverter} using the previously installed {@link
 * org.jodconverter.core.office.OfficeManager} under the hood for each conversion.
 *
 * @see org.jodconverter.core.DocumentConverter
 * @see org.jodconverter.core.office.OfficeManager
 * @see org.jodconverter.core.office.InstalledOfficeManagerHolder
 */
public final class JodConverter { // NOPMD - Disable utility class name rule violation

  public static void main(String[] args) throws OfficeException, UnsupportedEncodingException {
    //    File inputFile = new File("C:\\tmp\\jodc\\in\\test.odt");
    //    File outputFile = new File("C:\\tmp\\jodc\\out\\accenté_+.html");

    File inputFile = new File("C:\\tmp\\jodc\\out\\accenté_+.html");
    File outputFile = new File("C:\\tmp\\jodc\\out2\\accenté_+.html");

    // DocumentFormat fmt = DocumentFormat.copy(DefaultDocumentFormatRegistry.HTML);
    System.out.println(
        URLDecoder.decode("accent%C3%A9_+_html_7bab8e0.png", StandardCharsets.UTF_8.toString()));

    LocalOfficeManager.Builder config = LocalOfficeManager.builder();
    LocalOfficeManager officeManager = config.build();
    try {
      officeManager.start();
      LocalConverter localConverter =
          LocalConverter.builder()
              .filterChain(new LinkedImagesHtmlEncodingFilter())
              .officeManager(officeManager)
              .build();
      localConverter.convert(inputFile).to(outputFile).execute();
    } finally {
      officeManager.stop();
    }
  }

  /**
   * Converts a source file that is stored on the local file system.
   *
   * @param source The conversion input as a file.
   * @return The current conversion specification.
   */
  public static @NonNull ConversionJobWithOptionalSourceFormatUnspecified convert(
      final @NonNull File source) {

    return LocalConverter.make().convert(source);
  }

  /**
   * Converts a source stream input stream.
   *
   * @param source The conversion input as an input stream.
   * @return The current conversion specification.
   */
  public static @NonNull ConversionJobWithOptionalSourceFormatUnspecified convert(
      final @NonNull InputStream source) {

    return LocalConverter.make().convert(source);
  }

  /**
   * Converts a source stream input stream.
   *
   * @param source The conversion input as an input stream.
   * @param closeStream Whether the {@link InputStream} is closed after the conversion terminates.
   * @return The current conversion specification.
   */
  public static @NonNull ConversionJobWithOptionalSourceFormatUnspecified convert(
      final @NonNull InputStream source, final boolean closeStream) {

    return LocalConverter.make().convert(source, closeStream);
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private JodConverter() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
