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

package org.jodconverter.core.document;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.util.AssertUtils;

/** A SimpleDocumentFormatRegistry contains a collection of document formats supported by office. */
public class SimpleDocumentFormatRegistry implements DocumentFormatRegistry {

  private final Map<String, DocumentFormat> fmtsByExtension = new HashMap<>();
  private final Map<String, DocumentFormat> fmtsByMediaType = new HashMap<>();

  /**
   * Add a new format to the registry.
   *
   * @param documentFormat The format to add.
   */
  public void addFormat(final @NonNull DocumentFormat documentFormat) {

    documentFormat.getExtensions().stream()
        .map(s -> s.toLowerCase(Locale.ROOT))
        .forEach(ext -> fmtsByExtension.put(ext, documentFormat));
    fmtsByMediaType.put(documentFormat.getMediaType().toLowerCase(Locale.ROOT), documentFormat);
  }

  /**
   * Add all the formats from a registry to this registry, overwriting the existing entry.
   *
   * @param registry The registry to add to this registry.
   */
  public void addRegistry(final @Nullable SimpleDocumentFormatRegistry registry) {

    if (registry != null) {
      registry.fmtsByMediaType.values().forEach(this::addFormat);
    }
  }

  @Override
  public @Nullable DocumentFormat getFormatByExtension(final @NonNull String extension) {

    AssertUtils.notNull(extension, "extension must not be null");
    return fmtsByExtension.get(extension.toLowerCase(Locale.ROOT));
  }

  @Override
  public @Nullable DocumentFormat getFormatByMediaType(final @NonNull String mediaType) {

    AssertUtils.notNull(mediaType, "mediaType must not be null");
    return fmtsByMediaType.get(mediaType.toLowerCase(Locale.ROOT));
  }

  @Override
  public @NonNull Set<@NonNull DocumentFormat> getOutputFormats(
      final @NonNull DocumentFamily documentFamily) {

    AssertUtils.notNull(documentFamily, "documentFamily must not be null");
    // Use fmtsByMediaType since fmtsByExtension may contain the same
    // DocumentFormat with multiple extensions (e.g.: jpg, jpeg).
    return fmtsByMediaType.values().stream()
        .filter(format -> format.getStoreProperties(documentFamily) != null)
        .collect(Collectors.toSet());
  }
}
