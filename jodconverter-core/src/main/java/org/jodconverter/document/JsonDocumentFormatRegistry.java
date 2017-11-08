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

package org.jodconverter.document;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collection;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * A JsonDocumentFormatRegistry contains a collection of {@code DocumentFormat} supported by office
 * that has been loaded loaded from a JSON source.
 */
public class JsonDocumentFormatRegistry extends SimpleDocumentFormatRegistry {

  /**
   * Creates a JsonDocumentFormatRegistry from the given InputStream.
   *
   * @param source The InputStream (JSON format) containing the DocumentFormat collection.
   * @return The created JsonDocumentFormatRegistry.
   * @throws IOException If an I/O error occurs.
   */
  public static JsonDocumentFormatRegistry create(final InputStream source) throws IOException {

    return create(IOUtils.toString(source, "UTF-8"));
  }

  /**
   * Creates a JsonDocumentFormatRegistry from the given source.
   *
   * @param source The string (JSON format) containing the DocumentFormat collection.
   * @return The created JsonDocumentFormatRegistry.
   */
  public static JsonDocumentFormatRegistry create(final String source) {

    final JsonDocumentFormatRegistry registry = new JsonDocumentFormatRegistry();
    registry.readJsonArray(source);
    return registry;
  }

  // Force static function call
  protected JsonDocumentFormatRegistry() { // NOSONAR
    super();
  }

  // Fill the registry from the given JSON source
  protected void readJsonArray(final String source) {

    final Gson gson = new Gson();

    // Deserialization
    final Type collectionType = new TypeToken<Collection<DocumentFormat>>() {}.getType();
    final Collection<DocumentFormat> formats = gson.fromJson(source, collectionType);

    // Fill the registry with loaded formats. Note that we have to use
    // the constructor in order top create read only formats.
    for (final DocumentFormat format : formats) {
      addFormat(
          new DocumentFormat(
              format.getName(),
              format.getExtension(),
              format.getMediaType(),
              format.getInputFamily(),
              format.getLoadProperties(),
              format.getStoreProperties(),
              true));
    }
  }
}
