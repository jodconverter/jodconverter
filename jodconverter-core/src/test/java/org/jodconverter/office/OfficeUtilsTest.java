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

package org.jodconverter.office;

import static org.jodconverter.office.OfficeUtils.toUrl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.junit.Test;

public class OfficeUtilsTest {

  /** Tests the OfficeUtils.toUrl function on unix OS. */
  @Test
  public void unixToUrl() {

    assumeTrue(SystemUtils.IS_OS_UNIX);

    assertEquals(toUrl(new File("/tmp/document.odt")), "file:///tmp/document.odt");
    assertEquals(
        toUrl(new File("/tmp/document with spaces.odt")),
        "file:///tmp/document%20with%20spaces.odt");
  }

  /** Tests the OfficeUtils.toUrl function on Windows OS. */
  @Test
  public void windowsToUrl() {

    assumeTrue(SystemUtils.IS_OS_WINDOWS);

    String tempDir = System.getProperty("java.io.tmpdir");
    final File tempDirFile = new File(tempDir);
    tempDir = FilenameUtils.normalizeNoEndSeparator(tempDir, true);

    assertEquals(
        toUrl(new File(tempDirFile, "document.odt")), "file:///" + tempDir + "/document.odt");
    assertEquals(
        toUrl(new File(tempDirFile, "document with spaces.odt")),
        "file:///" + tempDir + "/document%20with%20spaces.odt");
  }
}
