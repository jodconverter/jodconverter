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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jodconverter.core.test.util.AssertUtil;
import org.jodconverter.local.MockUnoRuntimeExtension;

/** Contains tests for the {@link Draw} class. */
@ExtendWith(MockUnoRuntimeExtension.class)
class DrawTest {

  @Test
  void classWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(Draw.class);
  }

  @Nested
  class IsDraw {

    @Test
    @SuppressWarnings("ConstantConditions")
    void withNull_ShouldThrowNullPointerException(final UnoRuntime unoRuntime) {

      given(unoRuntime.queryInterface(XServiceInfo.class, null)).willReturn(null);

      assertThatNullPointerException().isThrownBy(() -> Draw.isDraw(null));
    }

    @Test
    void withDrawDoc_ShouldReturnTrue(final UnoRuntime unoRuntime) {

      final XComponent component = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, component)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.DRAW_SERVICE)).willReturn(true);

      assertThat(Draw.isDraw(component)).isTrue();
    }

    @Test
    void withoutDrawDoc_ShouldReturnFalse(final UnoRuntime unoRuntime) {

      final XComponent component = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, component)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.DRAW_SERVICE)).willReturn(false);

      assertThat(Draw.isDraw(component)).isFalse();
    }
  }

  @Nested
  class IsImpress {

    @Test
    @SuppressWarnings("ConstantConditions")
    void withNull_ShouldThrowNullPointerException(final UnoRuntime unoRuntime) {

      given(unoRuntime.queryInterface(XServiceInfo.class, null)).willReturn(null);

      assertThatNullPointerException().isThrownBy(() -> Draw.isImpress(null));
    }

    @Test
    void withImpressDoc_ShouldReturnTrue(final UnoRuntime unoRuntime) {

      final XComponent component = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, component)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.IMPRESS_SERVICE)).willReturn(true);

      assertThat(Draw.isImpress(component)).isTrue();
    }

    @Test
    void withoutImpressDoc_ShouldReturnFalse(final UnoRuntime unoRuntime) {

      final XComponent component = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, component)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.IMPRESS_SERVICE)).willReturn(false);

      assertThat(Draw.isImpress(component)).isFalse();
    }
  }
}
