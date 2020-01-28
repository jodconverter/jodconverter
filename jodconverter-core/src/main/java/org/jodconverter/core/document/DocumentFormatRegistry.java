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

import java.util.Set;

/**
 * A class implementing this interface should keep a collection of document format supported by
 * office.
 */
public interface DocumentFormatRegistry {

  /**
   * Gets a document format for the specified extension.
   *
   * @param extension The extension whose document format will be returned.
   * @return The found document format, or {@code null} if no document format exists for the
   *     specified extension.
   */
  DocumentFormat getFormatByExtension(String extension);

  /**
   * Gets a document format for the specified media type.
   *
   * @param mediaType The media type whose document format will be returned.
   * @return The found document format, or {@code null} if no document format exists for the
   *     specified media type.
   */
  DocumentFormat getFormatByMediaType(String mediaType);

  /**
   * Gets all the {@link DocumentFormat}s of a given family.
   *
   * @param family The family whose document formats will be returned.
   * @return A set with all the document formats for the specified family.
   */
  Set<DocumentFormat> getOutputFormats(DocumentFamily family);
}
