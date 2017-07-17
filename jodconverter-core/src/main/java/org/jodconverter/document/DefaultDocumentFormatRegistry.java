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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Default {@code DocumentFormat} registry. It contains the list of {@code DocumentFormat} that
 * should be enough to cover most of our needs.
 *
 * <p>See <a
 * href="https://wiki.openoffice.org/wiki/Framework/Article/Filter/FilterList_OOo_3_0">OpenOffice
 * Filters Documentation</a>
 *
 * <p>See <a
 * href="http://opengrok.libreoffice.org/xref/core/filter/source/config/fragments/filters">LibreOffice
 * Filters</a> and <a
 * href="https://svn.apache.org/repos/asf/openoffice/trunk/main/filter/source/config/fragments/filters">OpenOffice
 * Filters</a>.
 */
public final class DefaultDocumentFormatRegistry extends JsonDocumentFormatRegistry {

  /**
   * The InstanceHolder inner class is used to initialize the static INSTANCE on demand (the
   * instance will be initialized the first time it is used). Working this way allow us to create
   * (and thus load default document format) the static INSTANCE only if needed.
   */
  private static class InstanceHolder { // NOSONAR
    public static DefaultDocumentFormatRegistry INSTANCE = create(); // NOSONAR
  }

  /**
   * Creates a DefaultDocumentFormatRegistry with default formats.
   *
   * @return The created DefaultDocumentFormatRegistry with default formats.
   */
  public static DefaultDocumentFormatRegistry create() {

    final DefaultDocumentFormatRegistry registry = new DefaultDocumentFormatRegistry();
    registry.loadDefaults();
    return registry;
  }

  /**
   * Gets the default instance of the class.
   *
   * @return The ResourceManager used at this class hierarchy level.
   */
  public static DefaultDocumentFormatRegistry getInstance() {
    return InstanceHolder.INSTANCE;
  }

  // Force static function call
  private DefaultDocumentFormatRegistry() { // NOSONAR
    super();
  }

  // Load all the default supported DocumentFormat.
  private void loadDefaults() {

    try (final InputStream input =
        DefaultDocumentFormatRegistry.class.getResourceAsStream("/document-formats.json")) {
      String json = IOUtils.toString(input, "UTF-8");
      readJsonArray(json);
    } catch (IOException ex) {
      throw new DocumentFormatRegistryException(
          "Unable to load the default document-formats.json configuration file", ex);
    }
  }
}
