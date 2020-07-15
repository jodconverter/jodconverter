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
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.sheet.XSpreadsheetDocument;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jodconverter.core.test.util.AssertUtil;
import org.jodconverter.local.MockUnoRuntimeExtension;

/** Contains tests for the {@link Calc} class. */
@ExtendWith(MockUnoRuntimeExtension.class)
class CalcTest {

  @Test
  void classWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(Calc.class);
  }

  @Nested
  class IsCalc {

    @Test
    @SuppressWarnings("ConstantConditions")
    void withNull_ShouldThrowNullPointerException(final UnoRuntime unoRuntime) {

      given(unoRuntime.queryInterface(XServiceInfo.class, null)).willReturn(null);

      assertThatNullPointerException().isThrownBy(() -> Calc.isCalc(null));
    }

    @Test
    void withCalcDoc_ShouldReturnTrue(final UnoRuntime unoRuntime) {

      final XComponent component = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, component)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.CALC_SERVICE)).willReturn(true);

      assertThat(Calc.isCalc(component)).isTrue();
    }

    @Test
    void withoutCalcDoc_ShouldReturnFalse(final UnoRuntime unoRuntime) {

      final XComponent component = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, component)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.CALC_SERVICE)).willReturn(false);

      assertThat(Calc.isCalc(component)).isFalse();
    }
  }

  @Nested
  class GetCalcDoc {

    @Test
    void withNull_ShouldReturnNull() {

      assertThat(Calc.getCalcDoc(null)).isNull();
    }

    @Test
    void withCalcDoc_ShouldReturnXSpreadsheetDocument(final UnoRuntime unoRuntime) {

      final XComponent component = mock(XComponent.class);
      final XSpreadsheetDocument spreadsheetDocument = mock(XSpreadsheetDocument.class);
      given(unoRuntime.queryInterface(XSpreadsheetDocument.class, component))
          .willReturn(spreadsheetDocument);

      assertThat(Calc.getCalcDoc(component)).isEqualTo(spreadsheetDocument);
    }

    @Test
    void withoutCalcDoc_ShouldReturnNull(final UnoRuntime unoRuntime) {

      final XComponent component = mock(XComponent.class);
      given(unoRuntime.queryInterface(XSpreadsheetDocument.class, component)).willReturn(null);

      assertThat(Calc.getCalcDoc(component)).isNull();
    }
  }
}
