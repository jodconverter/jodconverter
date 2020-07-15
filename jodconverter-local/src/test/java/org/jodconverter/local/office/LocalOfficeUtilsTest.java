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

package org.jodconverter.local.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.jodconverter.local.office.LocalOfficeUtils.toUrl;
import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.test.util.AssertUtil;
import org.jodconverter.core.util.OSUtils;
import org.jodconverter.core.util.StringUtils;
import org.jodconverter.local.MockUnoRuntimeExtension;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.UnoRuntime;
import org.jodconverter.local.process.FreeBSDProcessManager;
import org.jodconverter.local.process.MacProcessManager;
import org.jodconverter.local.process.UnixProcessManager;
import org.jodconverter.local.process.WindowsProcessManager;

/** Contains tests for the {@link LocalOfficeUtils} class. */
@ExtendWith(MockUnoRuntimeExtension.class)
class LocalOfficeUtilsTest {

  @Test
  void classWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(LocalOfficeUtils.class);
  }

  @Nested
  class FindBestProcessManager {

    @Test
    void onMac_ShouldReturnMacProcessManager() {
      Assumptions.assumeTrue(OSUtils.IS_OS_MAC);

      assertThat(LocalOfficeUtils.findBestProcessManager())
          .isEqualTo(MacProcessManager.getDefault());
    }

    @Test
    void onFreeBSD_ShouldReturnFreeBSDProcessManager() {
      Assumptions.assumeTrue(OSUtils.IS_OS_FREE_BSD);

      assertThat(LocalOfficeUtils.findBestProcessManager())
          .isEqualTo(FreeBSDProcessManager.getDefault());
    }

    @Test
    void onUnix_ShouldReturnUnixProcessManager() {
      Assumptions.assumeTrue(OSUtils.IS_OS_UNIX);

      assertThat(LocalOfficeUtils.findBestProcessManager())
          .isEqualTo(UnixProcessManager.getDefault());
    }

    @Test
    void onWindows_ShouldReturnWindowsProcessManager() {
      Assumptions.assumeTrue(OSUtils.IS_OS_WINDOWS);

      assertThat(LocalOfficeUtils.findBestProcessManager())
          .isEqualTo(WindowsProcessManager.getDefault());
    }
  }

  @Nested
  class BuildOfficeUrls {

    @Test
    void withNullPortNumberAndNullPipeName_ShouldReturnOfficeUrlWithDefaultPortNumber() {

      assertThat(LocalOfficeUtils.buildOfficeUrls(null, null))
          .hasSize(1)
          .satisfies(
              urls ->
                  assertThat(urls.get(0).getConnectionAndParametersAsString())
                      .isEqualTo(
                          new OfficeUrl(LocalOfficeUtils.DEFAULT_PORT)
                              .getConnectionAndParametersAsString()));
    }

    @Test
    void withNullPortNumberAndEmptyPipeName_ShouldReturnOfficeUrlWithDefaultPortNumber() {

      assertThat(LocalOfficeUtils.buildOfficeUrls(null, new ArrayList<>()))
          .hasSize(1)
          .satisfies(
              urls ->
                  assertThat(urls.get(0).getConnectionAndParametersAsString())
                      .isEqualTo(
                          new OfficeUrl(LocalOfficeUtils.DEFAULT_PORT)
                              .getConnectionAndParametersAsString()));
    }

    @Test
    void withEmptyPortNumberAndEmptyPipeName_ShouldReturnOfficeUrlWithDefaultPortNumber() {

      assertThat(LocalOfficeUtils.buildOfficeUrls(new ArrayList<>(), new ArrayList<>()))
          .hasSize(1)
          .satisfies(
              urls ->
                  assertThat(urls.get(0).getConnectionAndParametersAsString())
                      .isEqualTo(
                          new OfficeUrl(LocalOfficeUtils.DEFAULT_PORT)
                              .getConnectionAndParametersAsString()));
    }

    @Test
    void withEmptyPortNumberAndNullPipeName_ShouldReturnOfficeUrlWithDefaultPortNumber() {

      assertThat(LocalOfficeUtils.buildOfficeUrls(new ArrayList<>(), null))
          .hasSize(1)
          .satisfies(
              urls ->
                  assertThat(urls.get(0).getConnectionAndParametersAsString())
                      .isEqualTo(
                          new OfficeUrl(LocalOfficeUtils.DEFAULT_PORT)
                              .getConnectionAndParametersAsString()));
    }

    @Test
    void withPortNumbersOnly_ShouldReturnOfficeUrlsWithGivenPortNumbers() {

      final List<Integer> portNumbers = Stream.of(2003, 2004, 2005).collect(Collectors.toList());
      assertThat(LocalOfficeUtils.buildOfficeUrls(portNumbers, null))
          .hasSize(3)
          .satisfies(
              urls ->
                  assertThat(urls.get(0).getConnectionAndParametersAsString())
                      .isEqualTo(new OfficeUrl(2003).getConnectionAndParametersAsString()))
          .satisfies(
              urls ->
                  assertThat(urls.get(1).getConnectionAndParametersAsString())
                      .isEqualTo(new OfficeUrl(2004).getConnectionAndParametersAsString()))
          .satisfies(
              urls ->
                  assertThat(urls.get(2).getConnectionAndParametersAsString())
                      .isEqualTo(new OfficeUrl(2005).getConnectionAndParametersAsString()));
    }

    @Test
    void withPipeNamesOnly_ShouldReturnOfficeUrlsWithGivenPipeNames() {

      final List<String> pipeNames = Stream.of("oo1", "oo2", "oo3").collect(Collectors.toList());
      assertThat(LocalOfficeUtils.buildOfficeUrls(null, pipeNames))
          .hasSize(3)
          .satisfies(
              urls ->
                  assertThat(urls.get(0).getConnectionAndParametersAsString())
                      .isEqualTo(new OfficeUrl("oo1").getConnectionAndParametersAsString()))
          .satisfies(
              urls ->
                  assertThat(urls.get(1).getConnectionAndParametersAsString())
                      .isEqualTo(new OfficeUrl("oo2").getConnectionAndParametersAsString()))
          .satisfies(
              urls ->
                  assertThat(urls.get(2).getConnectionAndParametersAsString())
                      .isEqualTo(new OfficeUrl("oo3").getConnectionAndParametersAsString()));
    }
  }

  @Nested
  class GetDocumentFamilySilently {

    @Test
    void withTextDocument_ShouldReturnTextFamily(final UnoRuntime unoRuntime) {

      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.WRITER_SERVICE)).willReturn(true);

      assertThat(LocalOfficeUtils.getDocumentFamilySilently(document))
          .isEqualTo(DocumentFamily.TEXT);
    }

    @Test
    void withCalcDocument_ShouldReturnSpreadsheetFamily(final UnoRuntime unoRuntime) {

      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.CALC_SERVICE)).willReturn(true);

      assertThat(LocalOfficeUtils.getDocumentFamilySilently(document))
          .isEqualTo(DocumentFamily.SPREADSHEET);
    }

    @Test
    void withImpressDocument_ShouldReturnPresentationFamily(final UnoRuntime unoRuntime) {

      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.IMPRESS_SERVICE)).willReturn(true);

      assertThat(LocalOfficeUtils.getDocumentFamilySilently(document))
          .isEqualTo(DocumentFamily.PRESENTATION);
    }

    @Test
    void withDrawDocument_ShouldReturnDrawingFamily(final UnoRuntime unoRuntime) {

      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.DRAW_SERVICE)).willReturn(true);

      assertThat(LocalOfficeUtils.getDocumentFamilySilently(document))
          .isEqualTo(DocumentFamily.DRAWING);
    }

    @Test
    void withInvalidDocument_ShouldReturnNull(final UnoRuntime unoRuntime) {

      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(isA(String.class))).willReturn(false);

      assertThat(LocalOfficeUtils.getDocumentFamilySilently(document)).isNull();
    }
  }

  @Nested
  class GetDocumentFamily {

    @Test
    void withInvalidDocument_ShouldThrowOfficeException(final UnoRuntime unoRuntime) {

      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(isA(String.class))).willReturn(false);

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> LocalOfficeUtils.getDocumentFamily(document));
    }

    @Test
    void withValidDocument_ShouldNotThrowAnyException(final UnoRuntime unoRuntime) {

      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(isA(String.class))).willReturn(true);

      assertThatCode(() -> LocalOfficeUtils.getDocumentFamily(document)).doesNotThrowAnyException();
    }
  }

  @Nested
  class ToUnoProperties {

    @Test
    void shouldRecurseOnMapProperties() {

      final Map<String, Object> properties = new LinkedHashMap<>();
      properties.put("Prop1", "Value1");
      final Map<String, Object> embedded = new LinkedHashMap<>();
      embedded.put("EmbedProp1", "EmbedValue1");
      embedded.put("EmbedProp2", "EmbedValue2");
      properties.put("Prop2", embedded);

      final PropertyValue[] unoProps = LocalOfficeUtils.toUnoProperties(properties);
      assertThat(unoProps).hasSize(2);
      assertThat(unoProps[0]).extracting("Name", "Value").containsExactly("Prop1", "Value1");
      assertThat(unoProps[1])
          .hasFieldOrPropertyWithValue("Name", "Prop2")
          .extracting("Value")
          .asInstanceOf(InstanceOfAssertFactories.ARRAY)
          .hasSize(2)
          .satisfies(
              arr -> {
                assertThat(arr[0])
                    .extracting("Name", "Value")
                    .containsExactly("EmbedProp1", "EmbedValue1");
                assertThat(arr[1])
                    .extracting("Name", "Value")
                    .containsExactly("EmbedProp2", "EmbedValue2");
              });
    }
  }

  @Nested
  class ToUrl {

    @Test
    void onUnix() {
      assumeTrue(OSUtils.IS_OS_UNIX);

      assertThat(toUrl(new File("/tmp/document.odt"))).isEqualTo("file:///tmp/document.odt");
      assertThat(toUrl(new File("/tmp/document with spaces.odt")))
          .isEqualTo("file:///tmp/document%20with%20spaces.odt");
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void onWindows(final @TempDir File testFolder) {
      assumeTrue(OSUtils.IS_OS_WINDOWS);

      String tempDir = testFolder.getPath();
      tempDir = StringUtils.appendIfMissing(tempDir, File.separator).replace('\\', '/');

      assertThat(toUrl(new File(testFolder, "document.odt")))
          .isEqualTo("file:///" + tempDir + "document.odt");
      assertThat(toUrl(new File(testFolder, "document with spaces.odt")))
          .isEqualTo("file:///" + tempDir + "document%20with%20spaces.odt");
    }
  }

  @Nested
  class ValidateOfficeHome {

    /** Tests the validateOfficeHome with non directory file as argument. */
    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void whenNotDirectory_ShouldThrowIllegalStateException(final @TempDir File testFolder)
        throws IOException {

      final File tempFile = new File(testFolder, "tmp");
      tempFile.createNewFile();
      assertThatIllegalStateException()
          .isThrownBy(() -> LocalOfficeUtils.validateOfficeHome(tempFile));
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void whenOfficeBinNotFound_ShouldThrowIllegalStateException(final @TempDir File testFolder) {

      final File officeHome = new File(testFolder, UUID.randomUUID().toString());
      officeHome.mkdirs();
      assertThatIllegalStateException()
          .isThrownBy(() -> LocalOfficeUtils.validateOfficeHome(officeHome));
    }
  }

  @Nested
  class ValidateOfficeTemplateProfileDir {

    @Test
    void withNull_ValidateSuccessfully() {

      assertThatCode(() -> LocalOfficeUtils.validateOfficeTemplateProfileDirectory(null))
          .doesNotThrowAnyException();
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void whenChildUserDirFound_ValidateSuccessfully(final @TempDir File testFolder) {

      final File profileDir = new File(testFolder, UUID.randomUUID().toString());
      new File(profileDir, "user").mkdirs();
      assertThatCode(() -> LocalOfficeUtils.validateOfficeTemplateProfileDirectory(profileDir))
          .doesNotThrowAnyException();
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void whenChildUserDirNotFound_ShouldThrowIllegalStateException(final @TempDir File testFolder) {

      final File profileDir = new File(testFolder, UUID.randomUUID().toString());
      profileDir.mkdirs();
      assertThatIllegalStateException()
          .isThrownBy(() -> LocalOfficeUtils.validateOfficeTemplateProfileDirectory(profileDir));
    }
  }
}
