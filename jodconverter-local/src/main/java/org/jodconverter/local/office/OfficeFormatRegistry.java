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

package org.jodconverter.local.office;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.document.DocumentFormatRegistry;

/**
 * A {@link DocumentFormatRegistry} that completes another registry with the formats of an office
 * installation: a format the other registry does not know is built from the filters the office said
 * it has (see {@link OfficeInfo}), so that a document can be converted to {@code epub} or to {@code
 * webp}, for example, without describing these formats.
 *
 * <pre>{@code
 * OfficeInfo info = OfficeInfoTask.query(officeManager);
 * LocalConverter converter =
 *     LocalConverter.builder()
 *         .officeManager(officeManager)
 *         .formatRegistry(OfficeFormatRegistry.create(info))
 *         .build();
 * }</pre>
 *
 * <p>The other registry always wins: its formats carry choices the office does not tell, such as
 * the filter options of a text file. A format built from the office has, for each family of
 * documents, the export filter of the office that writes the extension; when the office has more
 * than one, the first by name is taken. Describe the format in a registry of your own when another
 * one is wanted.
 *
 * <p>The registry is immutable: it describes the office as it was when it was asked.
 */
public final class OfficeFormatRegistry implements DocumentFormatRegistry {

  private static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";

  private static final Map<String, DocumentFamily> FAMILIES =
      Map.of(
          "com.sun.star.text.TextDocument", DocumentFamily.TEXT,
          "com.sun.star.text.GlobalDocument", DocumentFamily.TEXT,
          "com.sun.star.text.WebDocument", DocumentFamily.WEB,
          "com.sun.star.sheet.SpreadsheetDocument", DocumentFamily.SPREADSHEET,
          "com.sun.star.presentation.PresentationDocument", DocumentFamily.PRESENTATION,
          "com.sun.star.drawing.DrawingDocument", DocumentFamily.DRAWING);

  private final DocumentFormatRegistry registry;
  private final Map<String, DocumentFormat> officeFormatsByExtension = new LinkedHashMap<>();
  private final Map<String, DocumentFormat> officeFormatsByMediaType = new LinkedHashMap<>();

  /**
   * Creates a registry that completes the default registry with the formats of the given office.
   *
   * @param info The description of the office.
   * @return The registry.
   */
  public static @NonNull OfficeFormatRegistry create(final @NonNull OfficeInfo info) {
    return create(DefaultDocumentFormatRegistry.getInstance(), info);
  }

  /**
   * Creates a registry that completes the given registry with the formats of the given office.
   *
   * @param registry The registry that is asked first.
   * @param info The description of the office.
   * @return The registry.
   */
  public static @NonNull OfficeFormatRegistry create(
      final @NonNull DocumentFormatRegistry registry, final @NonNull OfficeInfo info) {
    return new OfficeFormatRegistry(registry, info);
  }

  private OfficeFormatRegistry(final DocumentFormatRegistry registry, final OfficeInfo info) {

    Objects.requireNonNull(registry, "registry must not be null");
    Objects.requireNonNull(info, "info must not be null");
    this.registry = registry;

    // The filters of each extension, in the order of the office description (by name).
    final var filtersByExtension = new LinkedHashMap<String, List<OfficeFilterInfo>>();
    for (final var filter : info.filters()) {
      if (familyOf(filter) != null) {
        for (final var extension : filter.extensions()) {
          filtersByExtension
              .computeIfAbsent(extension.toLowerCase(Locale.ROOT), key -> new ArrayList<>())
              .add(filter);
        }
      }
    }

    filtersByExtension.forEach(
        (extension, filters) -> {
          if (registry.getFormatByExtension(extension) == null) {
            final var format = toFormat(extension, filters);
            officeFormatsByExtension.put(extension, format);
            officeFormatsByMediaType.putIfAbsent(
                format.getMediaType().toLowerCase(Locale.ROOT), format);
          }
        });
  }

  private static @Nullable DocumentFamily familyOf(final OfficeFilterInfo filter) {
    return filter.documentService() == null ? null : FAMILIES.get(filter.documentService());
  }

  private static DocumentFormat toFormat(
      final String extension, final List<OfficeFilterInfo> filters) {

    // An export filter names the format better than an import one, when there is a choice.
    final var named =
        filters.stream().filter(OfficeFilterInfo::canExport).findFirst().orElse(filters.get(0));
    final var builder =
        DocumentFormat.builder()
            .name(named.uiName())
            .extension(extension)
            .mediaType(
                filters.stream()
                    .map(OfficeFilterInfo::mediaType)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(DEFAULT_MEDIA_TYPE));

    filters.stream()
        .filter(OfficeFilterInfo::canImport)
        .findFirst()
        .ifPresent(filter -> builder.inputFamily(familyOf(filter)));

    // The first export filter of each family.
    final var exported = new LinkedHashSet<DocumentFamily>();
    for (final var filter : filters) {
      final var family = familyOf(filter);
      if (filter.canExport() && exported.add(family)) {
        builder.storeFilterName(family, filter.name());
      }
    }
    return builder.build();
  }

  @Override
  public @Nullable DocumentFormat getFormatByExtension(final @NonNull String extension) {

    Objects.requireNonNull(extension, "extension must not be null");
    final var format = registry.getFormatByExtension(extension);
    return format == null
        ? officeFormatsByExtension.get(extension.toLowerCase(Locale.ROOT))
        : format;
  }

  @Override
  public @Nullable DocumentFormat getFormatByMediaType(final @NonNull String mediaType) {

    Objects.requireNonNull(mediaType, "mediaType must not be null");
    final var format = registry.getFormatByMediaType(mediaType);
    return format == null
        ? officeFormatsByMediaType.get(mediaType.toLowerCase(Locale.ROOT))
        : format;
  }

  @Override
  public @NonNull Set<@NonNull DocumentFormat> getOutputFormats(
      final @NonNull DocumentFamily family) {

    Objects.requireNonNull(family, "family must not be null");
    final var formats = new LinkedHashSet<>(registry.getOutputFormats(family));
    officeFormatsByExtension.values().stream()
        .filter(format -> format.getStoreProperties(family) != null)
        .forEach(formats::add);
    return Set.copyOf(formats);
  }
}
