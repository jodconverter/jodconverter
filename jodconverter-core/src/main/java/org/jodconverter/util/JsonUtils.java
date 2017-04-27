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

package org.jodconverter.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/** Helper class that provides JSON utilities. */
public final class JsonUtils {

  /**
   * Converts a JSONObject to a list.
   *
   * @param array the JSONArray to convert.
   * @return the created list from the JSONArray object.
   */
  public static List<Object> toList(final JSONArray array) {

    final List<Object> list = new ArrayList<>();
    for (int i = 0; i < array.length(); i++) {
      Object value = array.get(i);
      if (value instanceof JSONArray) {
        value = toList((JSONArray) value);
      } else if (value instanceof JSONObject) {
        value = toMap((JSONObject) value);
      }
      list.add(value);
    }
    return list;
  }

  /**
   * Converts a JSONObject to a map.
   *
   * @param object the JSONObject to convert.
   * @return the created map from the JSONObject object.
   */
  public static Map<String, Object> toMap(final JSONObject object) {

    if (object == null || object == JSONObject.NULL) {
      return null;
    }

    final Map<String, Object> map = new HashMap<>();

    final Iterator<String> keysItr = object.keySet().iterator();
    while (keysItr.hasNext()) {
      final String key = keysItr.next();
      Object value = object.get(key);

      if (value instanceof JSONArray) {
        value = toList((JSONArray) value);
      } else if (value instanceof JSONObject) {
        value = toMap((JSONObject) value);
      }
      map.put(key, value);
    }

    return map;
  }

  // Private ctor
  private JsonUtils() { // NOSONAR
    throw new AssertionError("Utility class must not be instantiated");
  }
}
