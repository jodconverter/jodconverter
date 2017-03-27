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

package org.artofsolving.jodconverter.office;

import static org.artofsolving.jodconverter.office.OfficeUtils.toUrl;
import static org.testng.Assert.assertEquals;

import java.io.File;

import org.testng.SkipException;
import org.testng.annotations.Test;

import org.artofsolving.jodconverter.util.PlatformUtils;

@Test(groups = "functional")
public class OfficeUtilsTest {

  // TODO create separate tests for Windows

  /** Tests the OfficeUtils.toUrl function. */
  public void unixToUrl() {

    if (PlatformUtils.isMac() || PlatformUtils.isWindows()) {
      throw new SkipException("Unix test can only be done on Unix");
    }

    assertEquals(toUrl(new File("/tmp/document.odt")), "file:///tmp/document.odt");
    assertEquals(
        toUrl(new File("/tmp/document with spaces.odt")),
        "file:///tmp/document%20with%20spaces.odt");
  }
}
