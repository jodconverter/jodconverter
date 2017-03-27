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

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

@Test
public class JsonDocumentFormatRegistryTest {

  /**
   * Test the readability of the JSON file that contains the supported document formats.
   *
   * @throws Exception if an error occurs.
   */
  public void readJsonRegistry() throws IOException {

    final InputStream input = getClass().getResourceAsStream("/document-formats.js");
    try {
      final DocumentFormatRegistry registry = JsonDocumentFormatRegistry.create(input);
      final DocumentFormat odt = registry.getFormatByExtension("odt");
      assertNotNull(odt);
      assertNotNull(odt.getStoreProperties(DocumentFamily.TEXT));
    } finally {
      IOUtils.closeQuietly(input);
    }
  }
}
