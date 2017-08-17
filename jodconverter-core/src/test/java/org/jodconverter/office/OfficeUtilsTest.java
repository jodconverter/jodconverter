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

import static org.assertj.core.api.Assertions.assertThat;
import static org.jodconverter.office.OfficeUtils.toUrl;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.junit.Test;

import org.jodconverter.test.util.AssertUtil;

public class OfficeUtilsTest {

  @Test
  public void ctor_ClassWellDefined() throws Exception {

    AssertUtil.assertUtilityClassWellDefined(OfficeUtils.class);
  }

  /** Tests the OfficeUtils.toUrl function on unix OS. */
  @Test
  public void unixToUrl() {

    assumeTrue(SystemUtils.IS_OS_UNIX);

    assertThat(toUrl(new File("/tmp/document.odt"))).isEqualTo("file:///tmp/document.odt");
    assertThat(toUrl(new File("/tmp/document with spaces.odt")))
        .isEqualTo("file:///tmp/document%20with%20spaces.odt");
  }

  /** Tests the OfficeUtils.toUrl function on Windows OS. */
  @Test
  public void windowsToUrl() {

    assumeTrue(SystemUtils.IS_OS_WINDOWS);

    String tempDir = System.getProperty("java.io.tmpdir");
    final File tempDirFile = new File(tempDir);
    tempDir = FilenameUtils.normalizeNoEndSeparator(tempDir, true);

    assertThat(toUrl(new File(tempDirFile, "document.odt")))
        .isEqualTo("file:///" + tempDir + "/document.odt");
    assertThat(toUrl(new File(tempDirFile, "document with spaces.odt")))
        .isEqualTo("file:///" + tempDir + "/document%20with%20spaces.odt");
  }

  /** Tests the validateOfficeHome with null as argument. */
  @Test(expected = IllegalStateException.class)
  public void validateOfficeHome_WithNullOfficeHome_ThrowsIllegalStateException() {

    OfficeUtils.validateOfficeHome(null);
  }

  /** Tests the validateOfficeHome with non directory file as argument. */
  @Test(expected = IllegalStateException.class)
  public void validateOfficeHome_WithNonDirectoryOfficeHome_ThrowsIllegalStateException()
      throws IOException {

    final File tempFile = File.createTempFile("OfficeUtilsTest", "tmp");
    tempFile.deleteOnExit();

    OfficeUtils.validateOfficeHome(tempFile);
  }

  /** Tests the validateOfficeHome when office bin is not found. */
  @Test(expected = IllegalStateException.class)
  public void validateOfficeHome_WithOfficeBinNotFound_ThrowsIllegalStateException() {

    final File tempDir = new File(System.getProperty("java.io.tmpdir"));
    final File officeHome = new File(tempDir, UUID.randomUUID().toString());
    try {
      officeHome.mkdirs();
      OfficeUtils.validateOfficeHome(officeHome);
    } finally {
      FileUtils.deleteQuietly(officeHome);
    }
  }

  /** Tests the validateOfficeTemplateProfileDirectory with null as argument. */
  public void validateOfficeTemplateProfileDir_WithNullDir_ValidateSuccessfully() {

    OfficeUtils.validateOfficeTemplateProfileDirectory(null);
    assertTrue(true);
  }

  /** Tests the validateOfficeTemplateProfileDirectory when user sub directory is found. */
  public void validateOfficeTemplateProfileDir_WithUserDirFound_ValidateSuccessfully() {

    final File tempDir = new File(System.getProperty("java.io.tmpdir"));
    final File templateProfileDir = new File(tempDir, UUID.randomUUID().toString());
    final File userDir = new File(templateProfileDir, "user");
    try {
      userDir.mkdirs();
      OfficeUtils.validateOfficeTemplateProfileDirectory(templateProfileDir);
    } finally {
      FileUtils.deleteQuietly(templateProfileDir);
    }
  }

  /** Tests the validateOfficeTemplateProfileDirectory when user sub directory is not found. */
  @Test(expected = IllegalStateException.class)
  public void validateOfficeTemplateProfileDir_WithUserDirNotFound_ThrowsIllegalStateException() {

    final File tempDir = new File(System.getProperty("java.io.tmpdir"));
    final File templateProfileDir = new File(tempDir, UUID.randomUUID().toString());
    try {
      templateProfileDir.mkdirs();
      OfficeUtils.validateOfficeTemplateProfileDirectory(templateProfileDir);
    } finally {
      FileUtils.deleteQuietly(templateProfileDir);
    }
  }

  /** Tests the validateOfficeWorkingDirectory with a file as argument. */
  @Test(expected = IllegalStateException.class)
  public void validateOfficeWorkingDirectory_WithFile_ThrowsIllegalStateException()
      throws IOException {

    final File tempFile = File.createTempFile("OfficeUtilsTest", "tmp");
    tempFile.deleteOnExit();

    OfficeUtils.validateOfficeWorkingDirectory(tempFile);
  }

  /** Tests the validateOfficeWorkingDirectory with an unexisting directory as argument. */
  @Test(expected = IllegalStateException.class)
  public void validateOfficeWorkingDirectory_WithUnexistingDirectory_ThrowsIllegalStateException()
      throws IOException {

    final File tempDir = new File(System.getProperty("java.io.tmpdir"));
    final File templateWorkingDir = new File(tempDir, UUID.randomUUID().toString());

    OfficeUtils.validateOfficeWorkingDirectory(templateWorkingDir);
  }

  /** Tests that an OfficeException is swallowed by the stopQuietly function. */
  @Test
  public void stopQuietly_OfficeExceptionThrown_ExceptionSwallowed() throws OfficeException {

    final OfficeManager officeManager = mock(OfficeManager.class);
    doThrow(OfficeException.class).when(officeManager).stop();

    OfficeUtils.stopQuietly(officeManager);
  }
}
