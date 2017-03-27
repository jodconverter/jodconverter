/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/** A SimpleDocumentFormatRegistry contains a collection of document formats supported by office. */
public class SimpleDocumentFormatRegistry implements DocumentFormatRegistry {

  private Map<String, DocumentFormat> fmtsByExtension = new HashMap<>();
  private Map<String, DocumentFormat> fmtsByMediaType = new HashMap<>();

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
