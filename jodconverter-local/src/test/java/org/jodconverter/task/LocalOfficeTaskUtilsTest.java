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

package org.jodconverter.task;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.UnoRuntime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.jodconverter.office.OfficeException;
import org.jodconverter.test.util.AssertUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UnoRuntime.class)
public class LocalOfficeTaskUtilsTest {

  @Test
  public void ctor_ClassWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(LocalOfficeTaskUtils.class);
  }

  @Test
  public void getDocumentFamily_WithNullDocument_ThrowNullPointerException() {

    assertThatNullPointerException().isThrownBy(() -> LocalOfficeTaskUtils.getDocumentFamily(null));
  }

  @Test
  public void getDocumentFamily_WithoutValidDocument_ThrowOfficeException() {

    final XServiceInfo serviceInfo = mock(XServiceInfo.class);
    given(serviceInfo.supportsService(isA(String.class))).willReturn(false);

    final XComponent document = mock(XComponent.class);
    mockStatic(UnoRuntime.class);
    given(UnoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(() -> LocalOfficeTaskUtils.getDocumentFamily(document));
  }
}
