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

package org.jodconverter.office.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

/**
 * A collection of utility functions to make Office info easier to get.
 *
 * <p>Inspired by the work of Dr. Andrew Davison from the website <a
 * href="http://fivedots.coe.psu.ac.th/~ad/jlop">Java LibreOffice Programming</a>.
 */
public final class Info {

  private static final Logger LOGGER = LoggerFactory.getLogger(Info.class);

  private static final String NODE_PRODUCT = "/org.openoffice.Setup/Product";
  private static final String NODE_L10N = "/org.openoffice.Setup/L10N";
  // private static final String NODE_OFFICE = "/org.openoffice.Setup/Office";

  private static final String[] NODE_PATHS = {NODE_PRODUCT, NODE_L10N};

  /**
   * Gets whether the specified context is for an OpenOffice installation.
   *
   * @param context The context.
   * @throws WrappedUnoException If an UNO exception occurs. The UNO exception will be the cause of
   *     the {@link WrappedUnoException}.
   */
  public static boolean isOpenOffice(final XComponentContext context) {
    return "openoffice".equalsIgnoreCase(getOfficeName(context));
  }

  /**
   * Gets whether the specified context is for a LibreOffice installation.
   *
   * @param context The context.
   * @throws WrappedUnoException If an UNO exception occurs. The UNO exception will be the cause of
   *     the {@link WrappedUnoException}.
   */
  public static boolean isLibreOffice(final XComponentContext context) {
    return "libreoffice".equalsIgnoreCase(getOfficeName(context));
  }

  /**
   * Gets the office product name for the given context.
   *
   * @param context The context.
   * @return The office product name, or {@code} if it could not be retrieved.
   * @throws WrappedUnoException If an UNO exception occurs. The UNO exception will be the cause of
   *     the {@link WrappedUnoException}.
   */
  public static String getOfficeName(final XComponentContext context) {
    return getConfig(context, "ooName").orElse(null);
  }

  /**
   * Gets the office product version (long representation) for the given context, e.g 6.1.0.3
   *
   * @param context The context.
   * @return The office product version, or {@code} if it could not be retrieved.
   * @throws WrappedUnoException If an UNO exception occurs. The UNO exception will be the cause of
   *     the {@link WrappedUnoException}.
   */
  public static String getOfficeVersionLong(final XComponentContext context) {
    return getConfig(context, "ooSetupVersionAboutBox").orElse(null);
  }

  /**
   * Gets the office product version (short representation) for the given context, e.g 6.1
   *
   * @param context The context.
   * @return The office product version, or {@code} if it could not be retrieved.
   * @throws WrappedUnoException If an UNO exception occurs. The UNO exception will be the cause of
   *     the {@link WrappedUnoException}.
   */
  public static String getOfficeVersionShort(final XComponentContext context) {
    return getConfig(context, "ooSetupVersion").orElse(null);
  }

  /**
   * Compares two versions strings (ex. 1.6.1).
   *
   * @param version1 The first version to compare.
   * @param version2 The second version to compare.
   * @param length The version length for normalization.
   * @return -1 if version1 &lt; version2, 1 if version1 &gt; version2, 0 if version1 = version2.
   */
  public static int compareVersions(
      final String version1, final String version2, final int length) {

    if (version1 == null && version2 == null) {
      return 0;
    } else if (version1 == null) {
      return -1;
    } else if (version2 == null) {
      return 1;
    }

    final String[] numbers1 = normalizeVersion(version1, length).split("\\.");
    final String[] numbers2 = normalizeVersion(version2, length).split("\\.");

    for (int i = 0; i < numbers1.length; i++) {
      if (Integer.valueOf(numbers1[i]) < Integer.valueOf(numbers2[i])) {
        return -1;
      } else if (Integer.valueOf(numbers1[i]) > Integer.valueOf(numbers2[i])) {
        return 1;
      }
    }

    return 0;
  }

  /**
   * Normalizes a version string so that it has 'length' number of version numbers separated by '.'
   */
  private static String normalizeVersion(final String version, final int length) {

    final List<String> numbers = new ArrayList<>(Arrays.asList(version.split("\\.")));
    while (numbers.size() < length) {
      numbers.add("0");
    }

    return numbers.stream().collect(Collectors.joining("."));
  }

  /**
   * Gets the configuration value of the specified property.
   *
   * @param context The main context.
   * @param propName The property name of the property value to get.
   * @return An optional containing the property value.
   * @throws WrappedUnoException If an UNO exception occurs. The UNO exception will be the cause of
   *     the {@link WrappedUnoException}.
   */
  public static Optional<String> getConfig(final XComponentContext context, final String propName) {

    for (String nodePath : NODE_PATHS) {
      final Optional<Object> info = getConfig(context, nodePath, propName);
      if (info.isPresent()) {
        return info.map(String.class::cast);
      }
    }
    return Optional.empty();
  }

  /**
   * Gets the configuration value of the specified property for the specified path.
   *
   * @param context The main context.
   * @param nodePath The path for which the properties are get.
   * @param propName The property name of the property value to get.
   * @return An optional containing the property value.
   * @throws WrappedUnoException If an UNO exception occurs. The UNO exception will be the cause of
   *     the {@link WrappedUnoException}.
   */
  public static Optional<Object> getConfig(
      final XComponentContext context, final String nodePath, final String propName) {

    return getConfigProperties(context, nodePath)
        .map(props -> Props.getProperty(props, propName))
        .orElse(Optional.empty());
  }

  /**
   * Gets the configuration properties for the specified path.
   *
   * @param context The main context.
   * @param nodePath The path for which the properties are get.
   * @return An optional {@link XPropertySet} containing the configuration properties for the
   *     specified path.
   */
  public static Optional<XPropertySet> getConfigProperties(
      final XComponentContext context, final String nodePath) {

    // Create the configuration provider and remember it as a XMultiServiceFactory
    final XMultiServiceFactory provider =
        Lo.createInstanceMCF(
            context,
            XMultiServiceFactory.class,
            "com.sun.star.configuration.ConfigurationProvider");
    if (provider == null) {
      LOGGER.debug("Could not create configuration provider");
      return Optional.empty();
    }

    // Specifies the location of the view root in the configuration.
    try {
      return Optional.ofNullable(
          Lo.qi(
              XPropertySet.class,
              provider.createInstanceWithArguments(
                  "com.sun.star.configuration.ConfigurationAccess",
                  Props.makeProperties("nodepath", nodePath))));
    } catch (Exception ex) {
      LOGGER.debug("Unable to access config properties for: " + nodePath, ex);
    }

    return Optional.empty();
  }

  /**
   * Gets whether the given document is of the given document type.
   *
   * @param document The document.
   * @param documentType The document type to check.
   * @return {@code true} if the document is of the specified type, {@code true} otherwise.
   */
  public static boolean isDocumentType(final XComponent document, final String documentType) {
    return Lo.qi(XServiceInfo.class, document).supportsService(documentType);
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private Info() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
