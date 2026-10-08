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

package org.jodconverter.core.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link PropertyValues} class. */
class PropertyValuesTest {

  @Test
  void classWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(PropertyValues.class);
  }

  @Test
  void parse_ShouldGiveBooleansNumbersAndText() {

    assertThat(PropertyValues.parse("true")).isEqualTo(Boolean.TRUE);
    assertThat(PropertyValues.parse("TRUE")).isEqualTo(Boolean.TRUE);
    assertThat(PropertyValues.parse("False")).isEqualTo(Boolean.FALSE);
    assertThat(PropertyValues.parse("42")).isEqualTo(42);
    assertThat(PropertyValues.parse("-1")).isEqualTo(-1);
    assertThat(PropertyValues.parse("4.2")).isEqualTo("4.2");
    assertThat(PropertyValues.parse("yes")).isEqualTo("yes");
    assertThat(PropertyValues.parse("")).isEqualTo("");
  }
}
