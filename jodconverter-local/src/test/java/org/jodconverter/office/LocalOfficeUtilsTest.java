/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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
import static org.jodconverter.office.LocalOfficeUtils.toUrl;
import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.UnoRuntime;

import org.jodconverter.test.util.AssertUtil;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnit4.class)
@PrepareForTest(UnoRuntime.class)
public class LocalOfficeUtilsTest {

  @Test
  public void ctor_ClassWellDefined() throws Exception {
    AssertUtil.assertUtilityClassWellDefined(LocalOfficeUtils.class);
  }

  @Test(expected = NullPointerException.class)
  public void getDocumentFamily_WithNullDocument_ThrowNullPointerException()
      throws OfficeException {
    LocalOfficeUtils.getDocumentFamily(null);
  }

  @Test(expected = OfficeException.class)
  public void getDocumentFamily_WithoutValidDocument_ThrowOfficeException() throws OfficeException {

    final XServiceInfo serviceInfo = mock(XServiceInfo.class);
    given(serviceInfo.supportsService(isA(String.class))).willReturn(false);

    final XComponent document = mock(XComponent.class);
    mockStatic(UnoRuntime.class);
    given(UnoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);

    LocalOfficeUtils.getDocumentFamily(document);
  }

  /** Tests the LocalOfficeUtils.toUrl function on unix OS. */
  @Test
  public void unixToUrl() {

    assumeTrue(SystemUtils.IS_OS_UNIX);

    assertThat(toUrl(new File("/tmp/document.odt"))).isEqualTo("file:///tmp/document.odt");
    assertThat(toUrl(new File("/tmp/document with spaces.odt")))
        .isEqualTo("file:///tmp/document%20with%20spaces.odt");
  }

  /** Tests the LocalOfficeUtils.toUrl function on Windows OS. */
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

    LocalOfficeUtils.validateOfficeHome(null);
  }

  /** Tests the validateOfficeHome with non directory file as argument. */
  @Test(expected = IllegalStateException.class)
  public void validateOfficeHome_WithNonDirectoryOfficeHome_ThrowsIllegalStateException()
      throws IOException {

    final File tempFile = File.createTempFile("LocalOfficeUtilsTest", "tmp");
    tempFile.deleteOnExit();

    LocalOfficeUtils.validateOfficeHome(tempFile);
  }

  /** Tests the validateOfficeHome when office bin is not found. */
  @Test(expected = IllegalStateException.class)
  public void validateOfficeHome_WithOfficeBinNotFound_ThrowsIllegalStateException() {

    final File tempDir = new File(System.getProperty("java.io.tmpdir"));
    final File officeHome = new File(tempDir, UUID.randomUUID().toString());
    try {
      officeHome.mkdirs();
      LocalOfficeUtils.validateOfficeHome(officeHome);
    } finally {
      FileUtils.deleteQuietly(officeHome);
    }
  }

  /** Tests the validateOfficeTemplateProfileDirectory with null as argument. */
  public void validateOfficeTemplateProfileDir_WithNullDir_ValidateSuccessfully() {

    LocalOfficeUtils.validateOfficeTemplateProfileDirectory(null);
  }

  /** Tests the validateOfficeTemplateProfileDirectory when user sub directory is found. */
  public void validateOfficeTemplateProfileDir_WithUserDirFound_ValidateSuccessfully() {

    final File tempDir = new File(System.getProperty("java.io.tmpdir"));
    final File profileDir = new File(tempDir, UUID.randomUUID().toString());
    final File userDir = new File(profileDir, "user");
    try {
      userDir.mkdirs();
      LocalOfficeUtils.validateOfficeTemplateProfileDirectory(profileDir);
    } finally {
      FileUtils.deleteQuietly(profileDir);
    }
  }

  /** Tests the validateOfficeTemplateProfileDirectory when user sub directory is not found. */
  @Test(expected = IllegalStateException.class)
  public void validateOfficeTemplateProfileDir_WithUserDirNotFound_ThrowsIllegalStateException() {

    final File tempDir = new File(System.getProperty("java.io.tmpdir"));
    final File profileDir = new File(tempDir, UUID.randomUUID().toString());
    try {
      profileDir.mkdirs();
      LocalOfficeUtils.validateOfficeTemplateProfileDirectory(profileDir);
    } finally {
      FileUtils.deleteQuietly(profileDir);
    }
  }

  /** Tests the validateOfficeWorkingDirectory with a file as argument. */
  @Test(expected = IllegalStateException.class)
  public void validateOfficeWorkingDirectory_WithFile_ThrowsIllegalStateException()
      throws IOException {

    final File tempFile = File.createTempFile("LocalOfficeUtilsTest", "tmp");
    tempFile.deleteOnExit();

    LocalOfficeUtils.validateOfficeWorkingDirectory(tempFile);
  }

  /** Tests the validateOfficeWorkingDirectory with an unexisting directory as argument. */
  @Test(expected = IllegalStateException.class)
  public void validateOfficeWorkingDirectory_WithUnexistingDirectory_ThrowsIllegalStateException()
      throws IOException {

    final File tempDir = new File(System.getProperty("java.io.tmpdir"));
    final File workingDir = new File(tempDir, UUID.randomUUID().toString());

    LocalOfficeUtils.validateOfficeWorkingDirectory(workingDir);
  }

  /** Tests the validateOfficeWorkingDirectory with a read only dicrectory as argument fails. */
  @Test(expected = IllegalStateException.class)
  public void validateOfficeWorkingDirectory_WithReadOnlyDirectory_ThrowsIllegalStateException()
      throws IOException {

    final File tempDir = new File(System.getProperty("java.io.tmpdir"));
    final File workingDir = new File(tempDir, UUID.randomUUID().toString());
    workingDir.setWritable(false);

    LocalOfficeUtils.validateOfficeWorkingDirectory(workingDir);
  }
}
