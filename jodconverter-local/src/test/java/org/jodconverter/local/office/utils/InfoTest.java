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

package org.jodconverter.local.office.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link Info} class. */
@RunWith(PowerMockRunner.class)
@PrepareForTest(UnoRuntime.class)
@PowerMockRunnerDelegate(JUnit4.class)
public class InfoTest {

  @Test
  public void new_ClassWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(Info.class);
  }

  @Test
  public void compareVersions_BothNull_ReturnEquals() {
    assertThat(Info.compareVersions(null, null, 0)).isEqualTo(0);
  }

  @Test
  public void compareVersions_FirstNull_ReturnFirstLessThanSecond() {
    assertThat(Info.compareVersions(null, "0.0", 0)).isEqualTo(-1);
  }

  @Test
  public void compareVersions_SecondNull_ReturSecondLessThanFirst() {
    assertThat(Info.compareVersions("0.0", null, 0)).isEqualTo(1);
  }

  @Test
  public void compareVersions_FirstEqualsSecond_ReturnFirstEqualsSecond() {
    assertThat(Info.compareVersions("1.5", "1.5", 0)).isEqualTo(0);
  }

  @Test
  public void compareVersions_FirstLessThanSecond_ReturnFirstLessThanSecond() {
    assertThat(Info.compareVersions("1.5", "1.6", 0)).isEqualTo(-1);
  }

  @Test
  public void compareVersions_SecondLessThanSecond_ReturSecondLessThanFirst() {
    assertThat(Info.compareVersions("1.6", "1.5", 0)).isEqualTo(1);
  }

  @Test
  public void compareVersionsNormalizing_FirstEqualsSecond_ReturnFirstEqualsSecond() {
    assertThat(Info.compareVersions("1.5.0", "1.5", 4)).isEqualTo(0);
  }

  @Test
  public void compareVersionsNormalizing_FirstLessThanSecond_ReturnFirstLessThanSecond() {
    assertThat(Info.compareVersions("1.5", "1.6.1", 4)).isEqualTo(-1);
  }

  @Test
  public void compareVersionsNormalizing_SecondLessThanSecond_ReturSecondLessThanFirst() {
    assertThat(Info.compareVersions("1.6.1", "1.5", 0)).isEqualTo(1);
  }

  @Test
  public void getConfig_WithUnoException_ReturnNull() throws Exception {

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
  public void getConfig_ProviderNotFound_ReturnNull() throws Exception {

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
