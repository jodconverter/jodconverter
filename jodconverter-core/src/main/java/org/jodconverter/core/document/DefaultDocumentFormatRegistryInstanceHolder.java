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

package org.jodconverter.core.document;

import java.io.IOException;
import java.io.InputStream;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Holds the default {@link DocumentFormatRegistry} instance. The {@link
 * DefaultDocumentFormatRegistry} will use this holder to initialize all its {@link DocumentFormat}
 * constants.
 */
public final
class DefaultDocumentFormatRegistryInstanceHolder { // NOPMD - Disable class name rule violation

  private static DocumentFormatRegistry instance;

  /**
   * Gets the default {@link DocumentFormatRegistry} instance.
   *
   * @return The default {@link DocumentFormatRegistry}.
   */
  public static @NonNull DocumentFormatRegistry getInstance() {
    synchronized (DocumentFormatRegistry.class) {
      if (instance == null) {
        final JsonDocumentFormatRegistry defaultRegistry = loadRegistry("/document-formats.json");
        if (defaultRegistry == null) {
          // It should never happen since the core module is shipped
          // with a default document-formats.json file.
          throw new DocumentFormatRegistryException(
              "Could not load the default document-formats.json configuration file");
        }

        // Now load the custom document formats, if any.
        defaultRegistry.addRegistry(loadRegistry("/custom-document-formats.json"));

        // Set the static instance.
        instance = defaultRegistry;
      }

      return instance;
    }
  }

  /**
   * Sets the default {@link DocumentFormatRegistry} instance.
   *
   * @param registry The default {@link DocumentFormatRegistry}.
   */
  public static void setInstance(DocumentFormatRegistry registry) {
    synchronized (DocumentFormatRegistry.class) {
      instance = registry;
    }
  }

  private static JsonDocumentFormatRegistry loadRegistry(final String name) {

    try (InputStream input =
        DefaultDocumentFormatRegistryInstanceHolder.class.getResourceAsStream(name)) {
      if (input != null) {
        return JsonDocumentFormatRegistry.create(input);
      }
    } catch (IOException ex) {
      throw new DocumentFormatRegistryException(
          "Could not load the configuration file: " + name, ex);
    }
    return null;
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private DefaultDocumentFormatRegistryInstanceHolder() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
