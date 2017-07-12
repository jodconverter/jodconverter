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

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Executable class that dumps a JSON version of the DefaultDocumentFormatRegistry. */
class DumpJsonDefaultDocumentFormatRegistry {

  private static final Logger logger =
      LoggerFactory.getLogger(DumpJsonDefaultDocumentFormatRegistry.class);

  private static class SortedJsonObject extends JSONObject {

    public SortedJsonObject() {

      super();
      try {
        final Field field = JSONObject.class.getDeclaredField("map");
        field.setAccessible(true);
        field.set(this, new LinkedHashMap<>());
      } catch (Exception ex) {
        // pass; will not be sorted
      }
    }
  }

  private static JSONObject toJson(final DocumentFormat format) throws JSONException {

    final JSONObject jsonFormat = new SortedJsonObject();
    jsonFormat.put("name", format.getName());
    jsonFormat.put("extension", format.getExtension());
    jsonFormat.put("mediaType", format.getMediaType());
    if (format.getInputFamily() != null) {
      jsonFormat.put("inputFamily", format.getInputFamily().name());
    }
    if (format.getLoadProperties() != null) {
      jsonFormat.put("loadProperties", toJson(format.getLoadProperties()));
    }
    if (format.getStoreProperties() != null) {
      final JSONObject jsonStoreProperties = new SortedJsonObject();
      for (final Map.Entry<DocumentFamily, Map<String, Object>> entry :
          format.getStoreProperties().entrySet()) {
        jsonStoreProperties.put(entry.getKey().name(), toJson(entry.getValue()));
      }
      jsonFormat.put("storeProperties", jsonStoreProperties);
    }
    return jsonFormat;
  }

  @SuppressWarnings("unchecked")
  private static JSONObject toJson(final Map<String, ?> properties) throws JSONException {
    final JSONObject jsonProperties = new SortedJsonObject();
    for (final Map.Entry<String, ?> entry : properties.entrySet()) {
      if (entry.getValue() instanceof Map) {
        final Map<String, ?> jsonValue = (Map<String, ?>) entry.getValue();
        jsonProperties.put(entry.getKey(), toJson(jsonValue));
      } else {
        jsonProperties.put(entry.getKey(), entry.getValue());
      }
    }
    return jsonProperties;
  }

  public static void main(final String[] args) throws Exception {

    final DefaultDocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();
    @SuppressWarnings("unchecked")
    final Map<String, DocumentFormat> formats =
        (Map<String, DocumentFormat>) FieldUtils.readField(registry, "fmtsByExtension", true);
    final JSONArray array = new JSONArray();
    for (final DocumentFormat format : formats.values()) {
      array.put(toJson(format));
    }
    logger.info(array.toString(2));
  }
}
