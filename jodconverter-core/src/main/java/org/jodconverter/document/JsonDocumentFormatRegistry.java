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

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import org.jodconverter.util.JsonUtils;

/**
 * A JsonDocumentFormatRegistry contains a collection of {@code DocumentFormat} supported by office
 * that has been loaded loaded from a JSON source.
 */
public final class JsonDocumentFormatRegistry extends SimpleDocumentFormatRegistry {

  /**
   * Creates a JsonDocumentFormatRegistry from the given InputStream.
   *
   * @param source the InputStream (JSON format) containing the DocumentFormat collection.
   * @return the created JsonDocumentFormatRegistry.
   * @throws IOException if an I/O error occurs.
   */
  public static JsonDocumentFormatRegistry create(final InputStream source) throws IOException {

    return create(IOUtils.toString(source, "UTF-8"));
  }

  /**
   * Creates a JsonDocumentFormatRegistry from the given source.
   *
   * @param source the string (JSON format) containing the DocumentFormat collection.
   * @return the created JsonDocumentFormatRegistry.
   */
  public static JsonDocumentFormatRegistry create(final String source) {

    final JsonDocumentFormatRegistry registry = new JsonDocumentFormatRegistry();
    registry.readJsonArray(source);
    return registry;
  }

  // Force static function call
  private JsonDocumentFormatRegistry() { // NOSONAR
    super();
  }

  // Fill the registry from the given JSON source
  private void readJsonArray(final String source) {

    final JSONArray array = new JSONArray(source);
    for (int i = 0; i < array.length(); i++) {
      final JSONObject jsonFormat = array.getJSONObject(i);
      final DocumentFormat format =
          new DocumentFormat(
              jsonFormat.getString("name"),
              jsonFormat.getString("extension"),
              jsonFormat.getString("mediaType"));
      if (jsonFormat.has("inputFamily")) {
        format.setInputFamily(DocumentFamily.valueOf(jsonFormat.getString("inputFamily")));
      }
      if (jsonFormat.has("loadProperties")) {
        format.setLoadProperties(JsonUtils.toMap(jsonFormat.getJSONObject("loadProperties")));
      }
      if (jsonFormat.has("storePropertiesByFamily")) {
        final JSONObject jsonStorePropertiesByFamily =
            jsonFormat.getJSONObject("storePropertiesByFamily");
        for (final String key : JSONObject.getNames(jsonStorePropertiesByFamily)) {
          format.setStoreProperties(
              DocumentFamily.valueOf(key),
              JsonUtils.toMap(jsonStorePropertiesByFamily.getJSONObject(key)));
        }
      }
      addFormat(format);
    }
  }
}
