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

package org.jodconverter.core.document;

import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Contains properties that will be applied when loading(opening) and storing(saving) document for a
 * specific {@link DocumentFormat}.
 */
public class DocumentFormatProperties {

  /** Properties applied when loading(opening) a document. */
  private final Map<String, Object> load = new HashMap<>();

  /** Properties applied when storing(saving) a document for each supported families. */
  private final Map<DocumentFamily, Map<String, Object>> store = new HashMap<>();

  /**
   * Gets the properties applied when loading(opening) a document.
   *
   * @return A map containing the properties.
   */
  @NonNull
  public Map<@NonNull String, @NonNull Object> getLoad() {
    return load;
  }

  /**
   * Gets the properties applied when storing(saving) a document for each supported families.
   *
   * @return A map containing the properties.
   */
  @NonNull
  public Map<@NonNull DocumentFamily, @NonNull Map<@NonNull String, @NonNull Object>> getStore() {
    return store;
  }
}
