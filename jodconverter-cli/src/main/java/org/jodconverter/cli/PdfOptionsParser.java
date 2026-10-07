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

package org.jodconverter.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jodconverter.core.document.PropertyValues;
import org.jodconverter.core.pdf.PdfOptions;

/**
 * Builds the {@link PdfOptions} of the command line, from the {@code --pdf-preset} and {@code
 * --pdf-option name=value} arguments. The option names and values are those of {@link
 * PdfOptions.Builder#option(String, String)}; the certificate, the private key and the certificate
 * authorities of a signature are given as files ({@code signature.certificate-file}...) whose PEM
 * content is read here, and {@code filter-data.Name=value} sets any {@code FilterData} property.
 */
final class PdfOptionsParser {

  private static final String FILTER_DATA_PREFIX = "filter-data.";
  private static final String CERTIFICATE_FILE = "signature.certificate-file";
  private static final String PRIVATE_KEY_FILE = "signature.private-key-file";
  private static final String CA_FILE = "signature.ca-file";

  // The command line gives the PEM files where the options take the PEM text.
  private static final Map<String, String> FILE_OPTIONS =
      Map.of(
          CERTIFICATE_FILE, "signature.certificate",
          PRIVATE_KEY_FILE, "signature.private-key",
          CA_FILE, "signature.ca");

  private static final Set<String> OPTION_NAMES = initOptionNames();

  // Suppresses default constructor, ensuring non-instantiability.
  private PdfOptionsParser() {
    throw new AssertionError("Utility class must not be instantiated");
  }

  /**
   * Gets the names accepted by the {@code --pdf-option} argument.
   *
   * @return The option names, in the order of their groups.
   */
  /* default */
  static Set<String> getOptionNames() {
    return OPTION_NAMES;
  }

  /**
   * Builds the PDF options described by the given arguments.
   *
   * @param preset The name of a preset to start from (archive, accessible or compact), or null.
   * @param options The options, each as "name=value", or null. They are applied in order, on top of
   *     the preset.
   * @return The PDF options, or null if there is neither a preset nor an option.
   * @throws IllegalArgumentException If a name or a value is not valid, or if the options cannot be
   *     used together.
   */
  /* default */
  static PdfOptions parse(final String preset, final String... options) {

    if (preset == null && (options == null || options.length == 0)) {
      return null;
    }

    final var builder = preset == null ? PdfOptions.builder() : preset(preset);
    String certificateFile = null;
    String privateKeyFile = null;
    if (options != null) {
      for (final var option : options) {
        final var separator = option.indexOf('=');
        if (separator <= 0) {
          throw new IllegalArgumentException(
              "Invalid PDF option '" + option + "'; expected name=value");
        }
        final var name = option.substring(0, separator).trim();
        final var value = option.substring(separator + 1);
        if (CERTIFICATE_FILE.equals(name)) {
          certificateFile = value;
        } else if (PRIVATE_KEY_FILE.equals(name)) {
          privateKeyFile = value;
        } else {
          apply(builder, name, value);
        }
      }
    }
    applyCertificate(builder, certificateFile, privateKeyFile);

    return builder.build();
  }

  private static PdfOptions.Builder preset(final String preset) {
    return switch (preset.trim().toUpperCase(Locale.ROOT)) {
      case "ARCHIVE" -> PdfOptions.archive().toBuilder();
      case "ACCESSIBLE" -> PdfOptions.accessible().toBuilder();
      case "COMPACT" -> PdfOptions.compact().toBuilder();
      default ->
          throw new IllegalArgumentException(
              "Unknown PDF preset '" + preset + "'; expected archive, accessible or compact");
    };
  }

  private static void apply(
      final PdfOptions.Builder builder, final String name, final String value) {

    if (name.startsWith(FILTER_DATA_PREFIX) && name.length() > FILTER_DATA_PREFIX.length()) {
      builder.filterData(name.substring(FILTER_DATA_PREFIX.length()), PropertyValues.parse(value));
    } else if (CA_FILE.equals(name)) {
      builder.option(FILE_OPTIONS.get(CA_FILE), readFile(value));
    } else if (FILE_OPTIONS.containsValue(name)) {
      // The PEM text options are reached through their file options only.
      throw unknownOption(name);
    } else {
      try {
        builder.option(name, value);
      } catch (IllegalArgumentException ex) {
        if (ex.getMessage() != null && ex.getMessage().startsWith("Unknown PDF option")) {
          throw unknownOption(name);
        }
        throw ex;
      }
    }
  }

  // The message names the options of the command line, with the file ones.
  private static IllegalArgumentException unknownOption(final String name) {
    return new IllegalArgumentException(
        "Unknown PDF option '" + name + "'; expected one of: " + String.join(", ", OPTION_NAMES));
  }

  // The certificate and its private key are two options, but must be set together.
  private static void applyCertificate(
      final PdfOptions.Builder builder, final String certificateFile, final String privateKeyFile) {

    if (certificateFile == null && privateKeyFile == null) {
      return;
    }
    if (certificateFile == null || privateKeyFile == null) {
      throw new IllegalArgumentException(
          "The PDF options '"
              + CERTIFICATE_FILE
              + "' and '"
              + PRIVATE_KEY_FILE
              + "' must be used together");
    }
    builder
        .option(FILE_OPTIONS.get(CERTIFICATE_FILE), readFile(certificateFile))
        .option(FILE_OPTIONS.get(PRIVATE_KEY_FILE), readFile(privateKeyFile));
  }

  private static String readFile(final String path) {
    try {
      return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new IllegalArgumentException("Could not read the file '" + path + "'", ex);
    }
  }

  // The names of the core options, with the PEM text ones replaced by their file options.
  private static Set<String> initOptionNames() {

    final var names = new LinkedHashSet<String>();
    for (final var name : PdfOptions.optionNames()) {
      names.add(
          FILE_OPTIONS.entrySet().stream()
              .filter(entry -> entry.getValue().equals(name))
              .map(Map.Entry::getKey)
              .findFirst()
              .orElse(name));
    }
    return Collections.unmodifiableSet(names);
  }
}
