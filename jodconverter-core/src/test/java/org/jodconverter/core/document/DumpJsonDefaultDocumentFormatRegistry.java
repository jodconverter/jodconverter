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

import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

/** Executable class that dumps a JSON version of the DefaultDocumentFormatRegistry. */
final class DumpJsonDefaultDocumentFormatRegistry {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(DumpJsonDefaultDocumentFormatRegistry.class);

  /**
   * Main entry point.
   *
   * @param args Program arguments.
   */
  public static void main(final String[] args) {

    final DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();
    @SuppressWarnings("unchecked")
    final TreeMap<String, DocumentFormat> formats =
        new TreeMap<>(
            (Map<String, DocumentFormat>)
                ReflectionTestUtils.getField(registry, "fmtsByExtension"));

    final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    LOGGER.info(gson.toJson(formats.values()));
  }

  // Private constructor.
  private DumpJsonDefaultDocumentFormatRegistry() {
    super();
  }
}
