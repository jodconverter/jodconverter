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

import java.util.Set;

/**
 * A class implementing this interface should keep a collection of document format supported by
 * office.
 */
public interface DocumentFormatRegistry {

  /**
   * Gets a document format for the specified extension.
   *
   * @param extension the extension whose document format will be returned.
   * @return the found document format, or {@code null} if no document format exists for the
   *     specified extension.
   */
  DocumentFormat getFormatByExtension(String extension);

  /**
   * Gets a document format for the specified media type.
   *
   * @param mediaType the media type whose document format will be returned.
   * @return the found document format, or {@code null} if no document format exists for the
   *     specified media type.
   */
  DocumentFormat getFormatByMediaType(String mediaType);

  /**
   * Gets all the {@link DocumentFormat}
   *
   * @param family the family whose document formats will be returned.
   * @return A set with all the document formats for the specified family.
   */
  Set<DocumentFormat> getOutputFormats(DocumentFamily family);
}
