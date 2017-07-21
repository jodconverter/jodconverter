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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

/** Contains the required information used to deal with a specific document format . */
public class DocumentFormat {

  /**
   * returns a copy of the specified {@link DocumentFormat}.
   *
   * @param from The document format from which the copy is created.
   * @return An exact copy of the specified format.
   */
  public static DocumentFormat copy(DocumentFormat from) {
    return new DocumentFormat(from);
  }

  private final String name;
  private final String extension;
  private final String mediaType;
  private DocumentFamily inputFamily;
  private Map<String, Object> loadProperties;
  private Map<DocumentFamily, Map<String, Object>> storeProperties;

  /**
   * Creates a new document format with the specified name, extension and mime-type.
   *
   * @param name The name of the format.
   * @param extension The extension of the format.
   * @param mediaType The media type (mime type) of the format.
   */
  public DocumentFormat(final String name, final String extension, final String mediaType) {

    this.name = name;
    this.extension = extension;
    this.mediaType = mediaType;
  }

  /**
   * Creates a copy of the specified document format.
   *
   * @param documentFormat The DocumentFormat from which the copy is made.
   */
  private DocumentFormat(final DocumentFormat documentFormat) {

    this.name = documentFormat.name;
    this.extension = documentFormat.extension;
    this.mediaType = documentFormat.mediaType;
    this.inputFamily = documentFormat.inputFamily;
    if (documentFormat.getLoadProperties() != null) {
      this.loadProperties = new HashMap<>(documentFormat.getLoadProperties());
    }
    if (documentFormat.getStoreProperties() != null) {
      storeProperties = new EnumMap<>(DocumentFamily.class);
      for (Map.Entry<DocumentFamily, Map<String, Object>> entry :
          documentFormat.getStoreProperties().entrySet()) {
        storeProperties.put(entry.getKey(), new HashMap<>(entry.getValue()));
      }
    }
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
   * Gets the properties required to store(save) a document of this format to a document of the
   * specified family.
   *
   * @param family The DocumentFamily for which the properties are get.
   * @return A map containing the properties to apply when storing a document of this format.
   */
  public Map<String, Object> getStoreProperties(final DocumentFamily family) {
    if (storeProperties == null) {
      return null;
    }
    return storeProperties.get(family);
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
   * Sets the DocumentFamily of the document.
   *
   * @param documentFamily The DocumentFamily of the document
   */
  public void setInputFamily(final DocumentFamily documentFamily) {
    this.inputFamily = documentFamily;
  }

  /**
   * Sets the properties required to load(open) a document of this format.
   *
   * @param loadProperties The new properties to set.
   */
  public void setLoadProperties(final Map<String, Object> loadProperties) {
    this.loadProperties = loadProperties;
  }

  /**
   * Sets the properties required to store(save) a document of this format to a document of another
   * family.
   *
   * @param family the DocumentFamily for which the properties are set.
   * @param storeProperties the new properties to set.
   */
  public void setStoreProperties(
      final DocumentFamily family, final Map<String, Object> storeProperties) {

    if (this.storeProperties == null) {
      this.storeProperties = new EnumMap<>(DocumentFamily.class);
    }
    this.storeProperties.put(family, storeProperties);
  }

  /**
   * Sets the properties required to store(save) a document of this format to a document of
   * supported families.
   *
   * @param storeProperties a DocumentFamily/Map pairs containing the properties to apply when
   *     storing a document of this format, by DocumentFamily.
   */
  public void setStoreProperties(final Map<DocumentFamily, Map<String, Object>> storeProperties) {

    this.storeProperties = storeProperties;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
