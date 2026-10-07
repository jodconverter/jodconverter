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

package org.jodconverter.cli;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.jodconverter.cli.CliConfigReader.Section;
import org.jodconverter.cli.CliConfigReader.Values;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.NoopFilter;
import org.jodconverter.local.filter.PagesSelectorFilter;
import org.jodconverter.local.filter.RefreshFilter;
import org.jodconverter.local.filter.text.DocumentIndexesUpdaterFilter;
import org.jodconverter.local.filter.text.DocumentInserterFilter;
import org.jodconverter.local.filter.text.GraphicInserterFilter;
import org.jodconverter.local.filter.text.LinkedImagesEmbedderFilter;
import org.jodconverter.local.filter.text.PageMarginsFilter;
import org.jodconverter.local.filter.text.TextInserterFilter;
import org.jodconverter.local.filter.text.TextReplacerFilter;

/**
 * Builds the filters of the {@code filters} section of a configuration file. Each entry of the list
 * is a map, with either a {@code type} key naming a built-in filter, or a {@code class} key naming
 * a custom {@link Filter} class with a public no-argument constructor. The built-in filters and
 * their keys are:
 *
 * <ul>
 *   <li>{@code noop}, {@code refresh}, {@code linked-images-embedder}: no key.
 *   <li>{@code pages-selector}: {@code pages}, a page number or a list of page numbers.
 *   <li>{@code document-inserter}: {@code document}, the path of the document to insert.
 *   <li>{@code graphic-inserter}: {@code image}, the path of the image; {@code horizontal-position}
 *       and {@code vertical-position} in millimeters, or {@code shape-properties}, a map of shape
 *       properties; optionally {@code width} and {@code height} in millimeters.
 *   <li>{@code text-inserter}: {@code text}, {@code width} and {@code height}; {@code
 *       horizontal-position} and {@code vertical-position}, or {@code shape-properties}.
 *   <li>{@code text-replacer}: {@code replacements}, a map of the texts to search and their
 *       replacements.
 *   <li>{@code page-margins}: {@code left}, {@code top}, {@code right} and {@code bottom} in
 *       millimeters, each optional.
 *   <li>{@code document-indexes-updater} (or its former name {@code table-of-content-updater}):
 *       {@code level}, the number of levels of the tables of contents, optional.
 * </ul>
 */
final class FilterParser {

  private static final String TYPE = "type";
  private static final String CLASS = "class";

  private static final Map<String, Function<Section, Filter>> TYPES = initTypes();

  // Suppresses default constructor, ensuring non-instantiability.
  private FilterParser() {
    throw new AssertionError("Utility class must not be instantiated");
  }

  /**
   * Gets the names of the built-in filters.
   *
   * @return The names accepted by the {@code type} key.
   */
  static Set<String> getTypeNames() {
    return TYPES.keySet();
  }

  /**
   * Builds the filters described by the given list.
   *
   * @param entries The entries of the {@code filters} section.
   * @return The filters, in the order of the list.
   * @throws IllegalArgumentException If an entry is not valid.
   */
  static List<Filter> parse(final List<?> entries) {

    final var filters = new ArrayList<Filter>();
    var index = 0;
    for (final var entry : entries) {
      index++;
      final var section = new Section("filter " + index, entry);
      final var filter =
          section.optional(TYPE).isPresent()
              ? builtIn(section, Values.string(section.required(TYPE), TYPE))
              : custom(section, Values.string(section.required(CLASS), CLASS));
      section.ensureNoUnknownKeys();
      filters.add(filter);
    }
    return filters;
  }

  private static Filter builtIn(final Section section, final String type) {

    final var factory = TYPES.get(type.trim().toLowerCase(Locale.ROOT).replace('_', '-'));
    if (factory == null) {
      throw new IllegalArgumentException(
          "Unknown filter type '"
              + type
              + "' in "
              + section.name()
              + "; expected one of: "
              + String.join(", ", TYPES.keySet()));
    }
    try {
      return factory.apply(section);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
          "Invalid filter '" + type + "' in " + section.name() + ": " + ex.getMessage(), ex);
    }
  }

  private static Filter custom(final Section section, final String className) {

    try {
      final var instance = Class.forName(className).getDeclaredConstructor().newInstance();
      if (instance instanceof Filter filter) {
        return filter;
      }
      throw new IllegalArgumentException(
          "The class '" + className + "' of " + section.name() + " is not a filter");
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException(
          "The class '" + className + "' of " + section.name() + " was not found", ex);
    } catch (NoSuchMethodException ex) {
      throw new IllegalArgumentException(
          "The class '"
              + className
              + "' of "
              + section.name()
              + " has no public no-argument constructor",
          ex);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
      throw new IllegalArgumentException(
          "The class '"
              + className
              + "' of "
              + section.name()
              + " could not be instantiated: "
              + (ex.getCause() == null ? ex : ex.getCause()).getMessage(),
          ex);
    }
  }

  private static Map<String, Function<Section, Filter>> initTypes() {

    final var map = new LinkedHashMap<String, Function<Section, Filter>>();
    map.put("noop", section -> new NoopFilter());
    map.put("refresh", section -> new RefreshFilter());
    map.put("linked-images-embedder", section -> new LinkedImagesEmbedderFilter());
    map.put("pages-selector", FilterParser::pagesSelector);
    map.put("document-inserter", FilterParser::documentInserter);
    map.put("graphic-inserter", FilterParser::graphicInserter);
    map.put("text-inserter", FilterParser::textInserter);
    map.put("text-replacer", FilterParser::textReplacer);
    map.put("page-margins", FilterParser::pageMargins);
    map.put("document-indexes-updater", FilterParser::documentIndexesUpdater);
    map.put("table-of-content-updater", FilterParser::documentIndexesUpdater);
    return map;
  }

  private static Filter pagesSelector(final Section section) {
    final var pages = Values.integers(section.required("pages"), "pages");
    return new PagesSelectorFilter(Arrays.stream(pages).boxed().toArray(Integer[]::new));
  }

  private static Filter documentInserter(final Section section) {
    return new DocumentInserterFilter(
        new File(Values.string(section.required("document"), "document")));
  }

  private static Filter graphicInserter(final Section section) {

    final var image = Values.string(section.required("image"), "image");
    final var size = size(section);
    final var shapeProperties = section.map("shape-properties").orElse(null);
    try {
      if (shapeProperties != null) {
        return size == null
            ? new GraphicInserterFilter(image, shapeProperties)
            : new GraphicInserterFilter(image, size[0], size[1], shapeProperties);
      }
      final var horizontal =
          Values.integer(section.required("horizontal-position"), "horizontal-position");
      final var vertical =
          Values.integer(section.required("vertical-position"), "vertical-position");
      return size == null
          ? new GraphicInserterFilter(image, horizontal, vertical)
          : new GraphicInserterFilter(image, size[0], size[1], horizontal, vertical);
    } catch (OfficeException ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    }
  }

  private static Filter textInserter(final Section section) {

    final var text = Values.string(section.required("text"), "text");
    final var width = Values.integer(section.required("width"), "width");
    final var height = Values.integer(section.required("height"), "height");
    final var shapeProperties = section.map("shape-properties").orElse(null);
    if (shapeProperties != null) {
      return new TextInserterFilter(text, width, height, shapeProperties);
    }
    return new TextInserterFilter(
        text,
        width,
        height,
        Values.integer(section.required("horizontal-position"), "horizontal-position"),
        Values.integer(section.required("vertical-position"), "vertical-position"));
  }

  private static Filter textReplacer(final Section section) {

    final var replacements =
        section
            .strings("replacements")
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "'replacements' is required in " + section.name()));
    if (replacements.isEmpty()) {
      throw new IllegalArgumentException("'replacements' must not be empty in " + section.name());
    }
    return new TextReplacerFilter(
        replacements.keySet().toArray(new String[0]), replacements.values().toArray(new String[0]));
  }

  private static Filter pageMargins(final Section section) {
    return new PageMarginsFilter(
        margin(section, "left"),
        margin(section, "top"),
        margin(section, "right"),
        margin(section, "bottom"));
  }

  private static Integer margin(final Section section, final String key) {
    return section.optional(key).map(value -> Values.integer(value, key)).orElse(null);
  }

  private static Filter documentIndexesUpdater(final Section section) {
    return new DocumentIndexesUpdaterFilter(
        section.optional("level").map(value -> Values.integer(value, "level")).orElse(0));
  }

  // The optional width and height, which must be given together.
  private static int[] size(final Section section) {
    final var width = section.optional("width").map(value -> Values.integer(value, "width"));
    final var height = section.optional("height").map(value -> Values.integer(value, "height"));
    if (width.isPresent() != height.isPresent()) {
      throw new IllegalArgumentException("'width' and 'height' must be given together");
    }
    return width.isPresent() ? new int[] {width.get(), height.get()} : null;
  }
}
