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

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.gson.annotations.SerializedName;

/** Contains the required information used to deal with a specific document format . */
public class DocumentFormat {

  private final String name;
  private final String extension;
  private final String mediaType;
  private final DocumentFamily inputFamily;
  private final Map<String, Object> loadProperties;

  @SerializedName(
    value = "storeProperties",
    alternate = {"storePropertiesByFamily"} // Be backward compatible
  )
  private final Map<DocumentFamily, Map<String, Object>> storeProperties;

  /**
   * Creates a new document format with the specified name, extension and mime-type.
   *
   * @param name The name of the format.
   * @param extension The extension of the format.
   * @param mediaType The media type (mime type) of the format.
   * @param inputFamily The DocumentFamily of the document.
   * @param loadProperties The properties required to load(open) a document of this format.
   * @param storeProperties The properties required to store(save) a document of this format to a
   *     document of another family.
   */
  public DocumentFormat(
      final String name,
      final String extension,
      final String mediaType,
      final DocumentFamily inputFamily,
      final Map<String, Object> loadProperties,
      final Map<DocumentFamily, Map<String, Object>> storeProperties) {

    this.name = name;
    this.extension = extension;
    this.mediaType = mediaType;
    this.inputFamily = inputFamily;

    this.loadProperties =
        loadProperties == null ? null : Collections.unmodifiableMap(new HashMap<>(loadProperties));

    Map<DocumentFamily, Map<String, Object>> storeProps = null;
    if (storeProperties != null) {
      storeProps = new EnumMap<>(DocumentFamily.class);
      for (final Map.Entry<DocumentFamily, Map<String, Object>> entry :
          storeProperties.entrySet()) {
        storeProps.put(
            entry.getKey(), Collections.unmodifiableMap(new HashMap<>(entry.getValue())));
      }
    }
    this.storeProperties = storeProperties == null ? null : Collections.unmodifiableMap(storeProps);
  }

  /**
   * Gets the extension associated with the format.
   *
   * @return A string that represents an extension.
   */
  public String getExtension() {
    return extension;
  }

  /**
   * Gets the DocumentFamily of the document.
   *
   * @return The DocumentFamily of the document.
   */
  public DocumentFamily getInputFamily() {
    return inputFamily;
  }

  /**
   * Gets the properties required to load(open) a document of this format.
   *
   * @return A map containing the properties to apply when loading a document of this format.
   */
  public Map<String, Object> getLoadProperties() {
    return loadProperties;
  }

  /**
   * Gets the media (mime) type of the format.
   *
   * @return A string that represents the media type.
   */
  public String getMediaType() {
    return mediaType;
  }

  /**
   * Gets the name of the format.
   *
   * @return A string that represents the name of the format.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the properties required to store(save) a document of this format to a document of
   * supported families.
   *
   * @return A DocumentFamily/Map pairs containing the properties to apply when storing a document
   *     of this format, by DocumentFamily.
   */
  public Map<DocumentFamily, Map<String, Object>> getStoreProperties() {
    return storeProperties;
  }

  /**
   * Gets the properties required to store(save) a document of this format to a document of the
   * specified family.
   *
   * @param family The DocumentFamily for which the properties are get.
   * @return A map containing the properties to apply when storing a document of this format.
   */
  public Map<String, Object> getStoreProperties(final DocumentFamily family) {

    return storeProperties == null ? null : storeProperties.get(family);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
