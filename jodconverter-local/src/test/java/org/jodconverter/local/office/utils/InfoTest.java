/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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

package org.jodconverter.local.office.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jodconverter.core.test.util.AssertUtil;
import org.jodconverter.local.MockUnoRuntimeExtension;

/** Contains tests for the {@link Info} class. */
@ExtendWith(MockUnoRuntimeExtension.class)
class InfoTest {

  @Test
  void classWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(Info.class);
  }

  @Nested
  class CompareVersions {

    @Test
    void withBothNull_ReturnEquals() {
      assertThat(Info.compareVersions(null, null, 0)).isEqualTo(0);
    }

    @Test
    void withFirstNull_ReturnFirstLessThanSecond() {
      assertThat(Info.compareVersions(null, "0.0", 0)).isEqualTo(-1);
    }

    @Test
    void withSecondNull_ReturSecondLessThanFirst() {
      assertThat(Info.compareVersions("0.0", null, 0)).isEqualTo(1);
    }

    @Test
    void withFirstEqualsSecond_ReturnFirstEqualsSecond() {
      assertThat(Info.compareVersions("1.5", "1.5", 0)).isEqualTo(0);
    }

    @Test
    void withFirstLessThanSecond_ReturnFirstLessThanSecond() {
      assertThat(Info.compareVersions("1.5", "1.6", 0)).isEqualTo(-1);
    }

    @Test
    void withSecondLessThanSecond_ReturSecondLessThanFirst() {
      assertThat(Info.compareVersions("1.6", "1.5", 0)).isEqualTo(1);
    }
  }

  @Nested
  class CompareVersionsNormalizing {

    @Test
    void withFirstEqualsSecond_ReturnFirstEqualsSecond() {
      assertThat(Info.compareVersions("1.5.0", "1.5", 4)).isEqualTo(0);
    }

    @Test
    void withFirstLessThanSecond_ReturnFirstLessThanSecond() {
      assertThat(Info.compareVersions("1.5", "1.6.1", 4)).isEqualTo(-1);
    }

    @Test
    void withSecondLessThanSecond_ReturSecondLessThanFirst() {
      assertThat(Info.compareVersions("1.6.1", "1.5", 0)).isEqualTo(1);
    }
  }

  @Nested
  class IsDocumentType {

    @Test
    void whenSupportsServiceReturnsTrue_ReturnTrue(final UnoRuntime unoRuntime) {

      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.WRITER_SERVICE)).willReturn(true);

      assertThat(Info.isDocumentType(document, Lo.WRITER_SERVICE)).isTrue();
    }

    @Test
    void whenSupportsServiceReturnsFalse_ReturnFalse(final UnoRuntime unoRuntime) {

      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.WRITER_SERVICE)).willReturn(false);

      assertThat(Info.isDocumentType(document, Lo.WRITER_SERVICE)).isFalse();
    }
  }

  @Nested
  class IsOpenOffice {

    @Test
    void whenNameIsOpenOffice_ReturnTrue(final UnoRuntime unoRuntime) throws Exception {

      final XComponentContext context = mock(XComponentContext.class);
      setUpConfigTest(unoRuntime, context, "ooName", "OpenOffice");
      assertThat(Info.isOpenOffice(context)).isTrue();
    }

    @Test
    void whenNameIsNotOpenOffice_ReturnTrue(final UnoRuntime unoRuntime) throws Exception {

      final XComponentContext context = mock(XComponentContext.class);
      setUpConfigTest(unoRuntime, context, "ooName", "LibreOffice");
      assertThat(Info.isOpenOffice(context)).isFalse();
    }
  }

  @Nested
  class IsLibreOffice {

    @Test
    void whenNameIsLibreOffice_ReturnTrue(final UnoRuntime unoRuntime) throws Exception {

      final XComponentContext context = mock(XComponentContext.class);
      setUpConfigTest(unoRuntime, context, "ooName", "LibreOffice");
      assertThat(Info.isLibreOffice(context)).isTrue();
    }

    @Test
    void whenNameIsNotLibreOffice_ReturnTrue(final UnoRuntime unoRuntime) throws Exception {

      final XComponentContext context = mock(XComponentContext.class);
      setUpConfigTest(unoRuntime, context, "ooName", "OpenOffice");
      assertThat(Info.isLibreOffice(context)).isFalse();
    }
  }

  @Nested
  class GetOfficeName {

    @Test
    void shouldReturnOONameProperty(final UnoRuntime unoRuntime) throws Exception {

      final XComponentContext context = mock(XComponentContext.class);
      setUpConfigTest(unoRuntime, context, "ooName", "foo");
      assertThat(Info.getOfficeName(context)).isEqualTo("foo");
    }

    @Test
    void shortVersion_ShouldReturnOOSetupVersionProperty(final UnoRuntime unoRuntime)
        throws Exception {

      final XComponentContext context = mock(XComponentContext.class);
      setUpConfigTest(unoRuntime, context, "ooSetupVersion", "6.1");
      assertThat(Info.getOfficeVersionShort(context)).isEqualTo("6.1");
    }
  }

  @Nested
  class GetOfficeVersion {

    @Test
    void longVersion_ShouldReturnOOSetupVersionAboutBoxProperty(final UnoRuntime unoRuntime)
        throws Exception {

      final XComponentContext context = mock(XComponentContext.class);
      setUpConfigTest(unoRuntime, context, "ooSetupVersionAboutBox", "6.1.0.3");
      assertThat(Info.getOfficeVersionLong(context)).isEqualTo("6.1.0.3");
    }

    @Test
    void shortVersion_ShouldReturnOOSetupVersionProperty(final UnoRuntime unoRuntime)
        throws Exception {

      final XComponentContext context = mock(XComponentContext.class);
      setUpConfigTest(unoRuntime, context, "ooSetupVersion", "6.1");
      assertThat(Info.getOfficeVersionShort(context)).isEqualTo("6.1");
    }
  }

  @Nested
  class GetConfig {

    @Test
    void whenUnoExceptionOccurs_ReturnNull() throws Exception {

      final XComponentContext context = mock(XComponentContext.class);
      final XMultiComponentFactory cfactory = mock(XMultiComponentFactory.class);
      final XMultiServiceFactory sfactory = mock(XMultiServiceFactory.class);

      given(context.getServiceManager()).willReturn(cfactory);
      given(
              cfactory.createInstanceWithContext(
                  "com.sun.star.configuration.ConfigurationProvider", context))
          .willReturn(sfactory);
      given(
              sfactory.createInstanceWithArguments(
                  eq("com.sun.star.configuration.ConfigurationAccess"), any(PropertyValue[].class)))
          .willThrow(Exception.class);

      assertThat(Info.getConfig(context, "unknownproperty")).isNull();
    }

    @Test
    void whenProviderNotFound_ReturnNull() throws Exception {

      final XComponentContext context = mock(XComponentContext.class);
      final XMultiComponentFactory cfactory = mock(XMultiComponentFactory.class);

      given(context.getServiceManager()).willReturn(cfactory);
      given(
              cfactory.createInstanceWithContext(
                  "com.sun.star.configuration.ConfigurationProvider", context))
          .willReturn(null);

      assertThat(Info.getConfig(context, "unknownproperty")).isNull();
    }
  }

  private void setUpConfigTest(
      final UnoRuntime unoRuntime,
      final XComponentContext context,
      final String propName,
      final Object propValue)
      throws Exception {

    final XMultiComponentFactory contextServiceManager = mock(XMultiComponentFactory.class);
    final Object provider = mock(Object.class);
    final XMultiServiceFactory providerServiceFactory = mock(XMultiServiceFactory.class);
    final Object configAccess = mock(Object.class);
    final XPropertySet propertySet = mock(XPropertySet.class);

    given(context.getServiceManager()).willReturn(contextServiceManager);
    given(
            contextServiceManager.createInstanceWithContext(
                "com.sun.star.configuration.ConfigurationProvider", context))
        .willReturn(provider);
    given(unoRuntime.queryInterface(XMultiServiceFactory.class, provider))
        .willReturn(providerServiceFactory);

    given(
            providerServiceFactory.createInstanceWithArguments(
                eq("com.sun.star.configuration.ConfigurationAccess"), any(PropertyValue[].class)))
        .willReturn(configAccess);
    given(unoRuntime.queryInterface(XPropertySet.class, configAccess)).willReturn(propertySet);
    given(propertySet.getPropertyValue(propName)).willReturn(propValue);
  }
}
