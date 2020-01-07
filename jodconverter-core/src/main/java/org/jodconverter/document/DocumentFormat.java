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

package org.jodconverter.document;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

/** Contains the required information used to deal with a specific document format . */
public class DocumentFormat {

  private final String name;
  // Be backward compatible. Former json file doesn't
  // support multiple document format extensions.
  @SerializedName(
      value = "extensions",
      alternate = {"extension"})
  @JsonAdapter(ExtensionsAdapter.class)
  private final List<String> extensions;

  private final String mediaType;
  private final DocumentFamily inputFamily;
  private final Map<String, Object> loadProperties;
  // Be backward compatible. storePropertiesByFamily
  // has been renamed storeProperties
  @SerializedName(
      value = "storeProperties",
      alternate = {"storePropertiesByFamily"})
  private final Map<DocumentFamily, Map<String, Object>> storeProperties;

  /**
   * Special adapter used to support backward compatibility when loading a document format json
   * file. Former json file doesn't support multiple document format extensions.
   */
  private static class ExtensionsAdapter implements JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(
        final JsonElement json, final Type type, final JsonDeserializationContext cxt) {

      if (json.isJsonArray()) {
        final Type listType = new TypeToken<List<String>>() {}.getType();
        return cxt.deserialize(json, listType);
      }
      return Stream.of(json.getAsString()).collect(Collectors.toList());
    }
  }

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new modifiable {@link DocumentFormat} from the specified format.
   *
   * @param sourceFormat The source document format.
   * @return A {@link DocumentFormat}, which will be modifiable, unlike the default document formats
   *     are.
   */
  public static DocumentFormat copy(final DocumentFormat sourceFormat) {

    return new Builder().from(sourceFormat).unmodifiable(false).build();
  }

  /**
   * Creates a new unmodifiable {@link DocumentFormat} from the specified format.
   *
   * @param sourceFormat The source document format.
   * @return A {@link DocumentFormat}, which will be unmodifiable, like the default document formats
   *     are.
   */
  public static DocumentFormat unmodifiableCopy(final DocumentFormat sourceFormat) {

    return new Builder().from(sourceFormat).unmodifiable(true).build();
  }

  /**
   * Creates a new read-only document format with the specified name, extension and mime-type.
   *
   * @param name The name of the format.
   * @param extensions The file name extensions of the format.
   * @param mediaType The media type (mime type) of the format.
   * @param inputFamily The DocumentFamily of the document.
   * @param loadProperties The properties required to load(open) a document of this format.
   * @param storeProperties The properties required to store(save) a document of this format to a
   *     document of another family.
   * @param unmodifiable {@code true} if the created document format cannot be modified after
   *     creation, {@code false} otherwise.
   */
  private DocumentFormat(
      final String name,
      final Collection<String> extensions,
      final String mediaType,
      final DocumentFamily inputFamily,
      final Map<String, Object> loadProperties,
      final Map<DocumentFamily, Map<String, Object>> storeProperties,
      final boolean unmodifiable) {

    this.name = name;
    this.extensions = new ArrayList<>(extensions);
    this.mediaType = mediaType;
    this.inputFamily = inputFamily;
    this.loadProperties =
        Optional.ofNullable(loadProperties)
            // Create a copy of the map.
            .map(HashMap::new)
            // Make the map read only if required.
            .map(mapCopy -> unmodifiable ? Collections.unmodifiableMap(mapCopy) : mapCopy)
            .orElse(null);
    this.storeProperties =
        Optional.ofNullable(storeProperties)
            // Create a copy of the map.
            .map(
                map -> {
                  final EnumMap<DocumentFamily, Map<String, Object>> familyMap =
                      new EnumMap<>(DocumentFamily.class);
                  map.forEach(
                      (family, propMap) ->
                          familyMap.put(
                              family,
                              unmodifiable
                                  ? Collections.unmodifiableMap(new HashMap<>(propMap))
                                  : new HashMap<>(propMap)));
                  return familyMap;
                })
            // Make the map read only if required.
            .map(mapCopy -> unmodifiable ? Collections.unmodifiableMap(mapCopy) : mapCopy)
            .orElse(null);
  }

  /**
   * Gets the extension associated with the document format. It will return the same extension as
   * {@code #getExtensions().get(0)}.
   *
   * @return A string that represents an extension.
   */
  public String getExtension() {
    return extensions.get(0);
  }

  /**
   * Gets the file name extensions of the document format.
   *
   * @return A list of string that represents the extensions.
   */
  public List<String> getExtensions() {
    return extensions;
  }

  /**
   * Gets the input DocumentFamily of the document format.
   *
   * @return The input DocumentFamily of the document format.
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
   * Gets the properties required to store(save) a document to this format from a document of the
   * specified family.
   *
   * @param family The DocumentFamily for which the properties are get.
   * @return A map containing the properties to apply when storing a document to this format.
   */
  public Map<String, Object> getStoreProperties(final DocumentFamily family) {

    return storeProperties == null ? null : storeProperties.get(family);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * A builder for constructing a {@link DocumentFormat}.
   *
   * @see DocumentFormat
   */
  public static final class Builder {

    private String name;
    private Set<String> extensions;
    private String mediaType;
    private DocumentFamily inputFamily;
    private Map<String, Object> loadProperties;
    private Map<DocumentFamily, Map<String, Object>> storeProperties;
    private boolean unmodifiable = true;

    // Private ctor so only DocumentFormat can initialize an instance of this builder.
    private Builder() {
      super();
    }

    /**
     * Creates the converter that is specified by this builder.
     *
     * @return The converter that is specified by this builder.
     */
    public DocumentFormat build() {

      return new DocumentFormat(
          name, extensions, mediaType, inputFamily, loadProperties, storeProperties, unmodifiable);
    }

    /**
     * Initializes the builder by copying the properties of the specified document format.
     *
     * @param sourceFormat The source document format, cannot be null.
     * @return This builder instance.
     */
    public Builder from(final DocumentFormat sourceFormat) {

      Validate.notNull(sourceFormat);
      this.name = sourceFormat.getName();
      this.extensions = new LinkedHashSet<>(sourceFormat.getExtensions());
      this.mediaType = sourceFormat.getMediaType();
      this.inputFamily = sourceFormat.getInputFamily();
      this.loadProperties =
          Optional.ofNullable(sourceFormat.getLoadProperties())
              .map(
                  map ->
                      map.entrySet().stream()
                          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
              .orElse(null);
      this.storeProperties =
          Optional.ofNullable(sourceFormat.getStoreProperties())
              .map(
                  map -> {
                    final EnumMap<DocumentFamily, Map<String, Object>> familyMap =
                        new EnumMap<>(DocumentFamily.class);
                    map.forEach((family, propMap) -> familyMap.put(family, new HashMap<>(propMap)));
                    return familyMap;
                  })
              .orElse(null);

      return this;
    }

    /**
     * Specifies the extension associated with the document format.
     *
     * @param extension The extension, cannot be null.
     * @return This builder instance.
     */
    public Builder extension(final String extension) {

      Validate.notBlank(extension);
      if (this.extensions == null) {
        this.extensions = new LinkedHashSet<>();
      }
      this.extensions.add(extension);
      return this;
    }

    /**
     * Specifies the input (when a document is loaded) DocumentFamily associated with the document
     * format.
     *
     * @param inputFamily The DocumentFamily, cannot be null.
     * @return This builder instance.
     */
    public Builder inputFamily(final DocumentFamily inputFamily) {

      Validate.notNull(inputFamily);
      this.inputFamily = inputFamily;
      return this;
    }

    /**
     * Adds a property to the builder that will be applied when loading (open) a document of this
     * format.
     *
     * @param name The property name, cannot be null.
     * @param value The property value, may be null. If null, it will REMOVE the property from the
     *     map.
     * @return This builder instance.
     */
    public Builder loadProperty(final String name, final Object value) {

      Validate.notBlank(name);

      if (value == null) {
        // Remove the property if the value is null.
        Optional.ofNullable(loadProperties).ifPresent(propMap -> propMap.remove(name));
      } else {
        // Add the property if a value is given.
        if (this.loadProperties == null) {
          this.loadProperties = new HashMap<>();
        }
        this.loadProperties.put(name, value);
      }

      return this;
    }

    /**
     * Specifies the media (mime) type of the document format.
     *
     * @param mediaType A string that represents the media type, cannot be null.
     * @return This builder instance.
     */
    public Builder mediaType(final String mediaType) {

      Validate.notBlank(mediaType);
      this.mediaType = mediaType;
      return this;
    }

    /**
     * Specifies the name of the document format.
     *
     * @param name The name of the document format, cannot be null.
     * @return This builder instance.
     */
    public Builder name(final String name) {

      Validate.notBlank(name);
      this.name = name;
      return this;
    }

    /**
     * Specifies whether the document format is unmodifiable after creation. Default to {@code
     * true}.
     *
     * @param unmodifiable {@code true} if the created document format cannot be modified after
     *     creation, {@code false} otherwise.
     * @return This builder instance.
     */
    public Builder unmodifiable(final boolean unmodifiable) {

      this.unmodifiable = unmodifiable;
      return this;
    }

    /**
     * Adds a property to the builder that will be applied when storing (save) a document to this
     * format from a document of the specified family.
     *
     * @param family The document family of the source (loaded) document, cannot be null.
     * @param name The property name, cannot be null.
     * @param value The property value, may be null. If null, it will REMOVE the property from the
     *     map.
     * @return This builder instance.
     */
    public Builder storeProperty(
        final DocumentFamily family, final String name, final Object value) {

      Validate.notBlank(name);
      Validate.notNull(family);

      if (value == null) {
        // Remove the property if the value is null.
        Optional.ofNullable(storeProperties)
            .map(familyMap -> familyMap.get(family))
            .ifPresent(propMap -> propMap.remove(name));
      } else {
        // Add the property if a value is given.
        if (storeProperties == null) {
          storeProperties = new EnumMap<>(DocumentFamily.class);
        }
        storeProperties.computeIfAbsent(family, key -> new HashMap<>()).put(name, value);
      }

      return this;
    }
  }
}
