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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/** A SimpleDocumentFormatRegistry contains a collection of document formats supported by office. */
public class SimpleDocumentFormatRegistry implements DocumentFormatRegistry {

  private final Map<String, DocumentFormat> fmtsByExtension = new HashMap<>();
  private final Map<String, DocumentFormat> fmtsByMediaType = new HashMap<>();

  /**
   * Add a new format to the registry.
   *
   * @param documentFormat the format to add.
   */
  public void addFormat(final DocumentFormat documentFormat) {

    fmtsByExtension.put(StringUtils.lowerCase(documentFormat.getExtension()), documentFormat);
    fmtsByMediaType.put(StringUtils.lowerCase(documentFormat.getMediaType()), documentFormat);
  }

  @Override
  public DocumentFormat getFormatByExtension(final String extension) {

    if (extension == null) {
      return null;
    }
    return fmtsByExtension.get(StringUtils.lowerCase(extension));
  }

  @Override
  public DocumentFormat getFormatByMediaType(final String mediaType) {

    if (mediaType == null) {
      return null;
    }
    return fmtsByMediaType.get(StringUtils.lowerCase(mediaType));
  }

  @Override
  public Set<DocumentFormat> getOutputFormats(final DocumentFamily family) {

    final Set<DocumentFormat> formats = new HashSet<>();

    if (family != null) {
      for (final DocumentFormat format : fmtsByExtension.values()) {
        if (format.getStoreProperties(family) != null) {
          formats.add(format);
        }
      }
    }
    return formats;
  }
}
