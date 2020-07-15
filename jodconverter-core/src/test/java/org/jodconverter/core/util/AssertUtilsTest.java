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

package org.jodconverter.core.util;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link AssertUtils} class. */
class AssertUtilsTest {

  @Test
  void classWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(AssertUtils.class);
  }

  @Nested
  class IsTrue {

    @Test
    @SuppressWarnings("ConstantConditions")
    void withFalseExpression_ShouldThrowIllegalArgumentsException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> AssertUtils.isTrue(false, "expression must be true"));
    }

    @Test
    void withTrueExpression_NoExceptionThrown() {

      assertThatCode(() -> AssertUtils.isTrue(true, "expression must be true"))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  class NotBlank {

    @Test
    @SuppressWarnings("ConstantConditions")
    void withNullString_ShouldThrowNullPointerException() {

      assertThatNullPointerException()
          .isThrownBy(() -> AssertUtils.notBlank(null, "string must not be null nor blank"));
    }

    @Test
    void withEmptyString_ShouldThrowIllegalArgumentsException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> AssertUtils.notBlank("", "string must not be null nor blank"));
    }

    @Test
    void withBlankString_ShouldThrowIllegalArgumentsException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> AssertUtils.notBlank("  ", "string must not be null nor blank"));
    }

    @Test
    void withNotBlankString_NoExceptionThrown() {

      assertThatCode(() -> AssertUtils.notBlank("  test  ", "string must not be null nor blank"))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  class NotEmpty {

    @Test
    @SuppressWarnings("ConstantConditions")
    void withNullString_ShouldThrowNullPointerException() {

      assertThatNullPointerException()
          .isThrownBy(
              () -> AssertUtils.notEmpty((String) null, "string must not be null nor empty"));
    }

    @Test
    void withEmptyString_ShouldThrowIllegalArgumentsException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> AssertUtils.notEmpty("", "string must not be null nor empty"));
    }

    @Test
    void withNotEmptyString_NoExceptionThrown() {

      assertThatCode(() -> AssertUtils.notEmpty("  ", "string must not be null nor empty"))
          .doesNotThrowAnyException();
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void withNullCollection_ShouldThrowNullPointerException() {

      assertThatNullPointerException()
          .isThrownBy(
              () ->
                  AssertUtils.notEmpty(
                      (Collection<?>) null, "collection must not be null nor empty"));
    }

    @Test
    void withEmptyCollection_ShouldThrowIllegalArgumentsException() {

      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  AssertUtils.notEmpty(new ArrayList<>(), "collection must not be null nor empty"));
    }

    @Test
    void withNotEmptyCollection_NoExceptionThrown() {

      assertThatCode(
              () ->
                  AssertUtils.notEmpty(
                      Arrays.stream(new Object[] {""}).collect(Collectors.toList()),
                      "collection must not be null nor empty"))
          .doesNotThrowAnyException();
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void withNullArray_ShouldThrowNullPointerException() {

      assertThatNullPointerException()
          .isThrownBy(
              () -> AssertUtils.notEmpty((Object[]) null, "array must not be null nor empty"));
    }

    @Test
    void withEmptyArray_ShouldThrowIllegalArgumentsException() {

      assertThatIllegalArgumentException()
          .isThrownBy(
              () -> AssertUtils.notEmpty(new Object[] {}, "array must not be null nor empty"));
    }

    @Test
    void withNotEmptyArray_NoExceptionThrown() {

      assertThatCode(
              () -> AssertUtils.notEmpty(new Object[] {""}, "array must not be null nor empty"))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  class NotNull {

    @Test
    @SuppressWarnings("ConstantConditions")
    void withNullObject_ShouldThrowNullPointerException() {

      assertThatNullPointerException()
          .isThrownBy(() -> AssertUtils.notNull(null, "object must not be null"));
    }

    @Test
    @SuppressWarnings("ObviousNullCheck")
    void withNotNullObject_NoExceptionThrown() {

      assertThatCode(() -> AssertUtils.notNull(new Object(), "object must not be null"))
          .doesNotThrowAnyException();
    }
  }
}
