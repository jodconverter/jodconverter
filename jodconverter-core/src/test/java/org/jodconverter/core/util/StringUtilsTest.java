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

package org.jodconverter.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link StringUtils} class. */
class StringUtilsTest {

  @Test
  void classWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(StringUtils.class);
  }

  @Nested
  class AppendIfMissing {

    @Test
    void withNullInputString_ShouldReturnNull() {
      assertThat(StringUtils.appendIfMissing(null, "/")).isNull();
    }

    @Test
    void withNullSuffix_ShouldReturnInputString() {
      assertThat(StringUtils.appendIfMissing("input", null)).isEqualTo("input");
    }

    @Test
    void withEmptySuffix_ShouldReturnInputString() {
      assertThat(StringUtils.appendIfMissing("input", "")).isEqualTo("input");
    }

    @Test
    void withNotMissingSuffix_ShouldReturnInputString() {
      assertThat(StringUtils.appendIfMissing("path/to/dir/", "/")).isEqualTo("path/to/dir/");
    }

    @Test
    void withMissingSuffix_ShouldReturnInputStringWithSuffix() {
      assertThat(StringUtils.appendIfMissing("path/to/dir", "/")).isEqualTo("path/to/dir/");
    }
  }

  @Nested
  class EndsWithAny {

    @Test
    void withNullString_ShouldReturnFalse() {
      assertThat(StringUtils.endsWithAny(null)).isFalse();
    }

    @Test
    void withEmptyString_ShouldReturnFalse() {
      assertThat(StringUtils.endsWithAny("")).isFalse();
    }

    @Test
    void withNullSearchStrings_ShouldReturnFalse() {
      assertThat(StringUtils.endsWithAny("input", (String[]) null)).isFalse();
    }

    @Test
    void withEmptySearchStrings_ShouldReturnFalse() {
      assertThat(StringUtils.endsWithAny("input")).isFalse();
    }

    @Test
    void withNullOrNotEndsWithSearchStrings_ShouldReturnFalse() {
      assertThat(StringUtils.endsWithAny("input", "test1", null, "test2")).isFalse();
    }

    @Test
    void withEndsWithSearchStrings_ShouldReturnTrue() {
      assertThat(StringUtils.endsWithAny("input", "test1", null, "ut")).isTrue();
    }
  }

  @Nested
  @SuppressWarnings("ConstantConditions")
  class IsEmpty {
    @Test
    void withNullString_ShouldReturnTrue() {
      assertThat(StringUtils.isEmpty(null)).isTrue();
    }

    @Test
    void withEmptyString_ShouldReturnTrue() {
      assertThat(StringUtils.isEmpty("")).isTrue();
    }

    @Test
    void withBlankString_ShouldReturnFalse() {
      assertThat(StringUtils.isEmpty(" ")).isFalse();
    }

    @Test
    void withNotEmptyString_ShouldReturnFalse() {
      assertThat(StringUtils.isEmpty("test")).isFalse();
    }
  }

  @Nested
  @SuppressWarnings("ConstantConditions")
  class IsNotEmpty {

    @Test
    void withNullString_ShouldReturnFalse() {
      assertThat(StringUtils.isNotEmpty(null)).isFalse();
    }

    @Test
    void withEmptyString_ShouldReturnFalse() {
      assertThat(StringUtils.isNotEmpty("")).isFalse();
    }

    @Test
    void withBlankString_ShouldReturnTrue() {
      assertThat(StringUtils.isNotEmpty(" ")).isTrue();
    }

    @Test
    void withNotEmptyString_ShouldReturnTrue() {
      assertThat(StringUtils.isNotEmpty("test")).isTrue();
    }
  }

  @Nested
  class IsBlank {

    @Test
    void withNullString_ShouldReturnTrue() {
      assertThat(StringUtils.isBlank(null)).isTrue();
    }

    @Test
    void withEmptyString_ShouldReturnTrue() {
      assertThat(StringUtils.isBlank("")).isTrue();
    }

    @Test
    void withBlankString_ShouldReturnTrue() {
      assertThat(StringUtils.isBlank("  \t  ")).isTrue();
    }

    @Test
    void withNotBlankString_ShouldReturnFalse() {
      assertThat(StringUtils.isBlank("  test\t  ")).isFalse();
    }
  }

  @Nested
  class IsNotBlank {
    @Test
    void withNullString_ShouldReturnFalse() {
      assertThat(StringUtils.isNotBlank(null)).isFalse();
    }

    @Test
    void withEmptyString_ShouldReturnFalse() {
      assertThat(StringUtils.isNotBlank("")).isFalse();
    }

    @Test
    void withBlankString_ShouldReturnFalse() {
      assertThat(StringUtils.isNotBlank("  \t  ")).isFalse();
    }

    @Test
    void withNotBlankString_ShouldReturnTrue() {
      assertThat(StringUtils.isNotBlank("  test\t  ")).isTrue();
    }
  }
}
