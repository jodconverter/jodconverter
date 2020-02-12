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
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.Nullable;

/** A SimpleDocumentFormatRegistry contains a collection of document formats supported by office. */
public class SimpleDocumentFormatRegistry implements DocumentFormatRegistry {

  private final Map<String, DocumentFormat> fmtsByExtension = new HashMap<>();
  private final Map<String, DocumentFormat> fmtsByMediaType = new HashMap<>();

  /**
   * Add a new format to the registry.
   *
   * @param documentFormat The format to add.
   */
  public void addFormat(final DocumentFormat documentFormat) {

    documentFormat.getExtensions().stream()
        .map(StringUtils::lowerCase)
        .forEach(ext -> fmtsByExtension.put(ext, documentFormat));
    fmtsByMediaType.put(StringUtils.lowerCase(documentFormat.getMediaType()), documentFormat);
  }

  @Nullable
  @Override
  public DocumentFormat getFormatByExtension(final String extension) {

    Validate.notNull(extension, "extension must not be null");
    return fmtsByExtension.get(StringUtils.lowerCase(extension));
  }

  @Nullable
  @Override
  public DocumentFormat getFormatByMediaType(final String mediaType) {

    Validate.notNull(mediaType, "mediaType must not be null");
    return fmtsByMediaType.get(StringUtils.lowerCase(mediaType));
  }

  @Override
  public Set<DocumentFormat> getOutputFormats(final DocumentFamily documentFamily) {

    Validate.notNull(documentFamily, "documentFamily must not be null");
    // Use fmtsByMediaType since fmtsByExtension may contain the same
    // DocumentFormat with multiple extensions (e.g: jpg, jpeg).
    return fmtsByMediaType.values().stream()
        .filter(format -> format.getStoreProperties(documentFamily) != null)
        .collect(Collectors.toSet());
  }
}
