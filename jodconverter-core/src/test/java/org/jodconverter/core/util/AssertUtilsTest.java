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

import org.junit.jupiter.api.Test;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link AssertUtils} class. */
public class AssertUtilsTest {

  @Test
  public void new_ClassWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(AssertUtils.class);
  }

  @Test
  public void isTrue_WithFalseExpression_ShouldThrowIllegalArgumentsException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> AssertUtils.isTrue(false, "expression must be true"));
  }

  @Test
  public void isTrue_WithTrue_NoExceptionThrown() {

    assertThatCode(() -> AssertUtils.isTrue(true, "expression must be true"))
        .doesNotThrowAnyException();
  }

  @Test
  public void notBlank_WithNullString_ShouldThrowNullPointerException() {

    assertThatNullPointerException()
        .isThrownBy(() -> AssertUtils.notBlank(null, "string must not be null nor blank"));
  }

  @Test
  public void notBlank_WithEmptyString_ShouldThrowIllegalArgumentsException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> AssertUtils.notBlank("", "string must not be null nor blank"));
  }

  @Test
  public void notBlank_WithBlankString_ShouldThrowIllegalArgumentsException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> AssertUtils.notBlank("  ", "string must not be null nor blank"));
  }

  @Test
  public void notBlank_WithNotBlankString_NoExceptionThrown() {

    assertThatCode(() -> AssertUtils.notBlank("  test  ", "string must not be null nor blank"))
        .doesNotThrowAnyException();
  }

  @Test
  public void notEmpty_WithNullString_ShouldThrowNullPointerException() {

    assertThatNullPointerException()
        .isThrownBy(() -> AssertUtils.notEmpty((String) null, "string must not be null nor empty"));
  }

  @Test
  public void notEmpty_WithEmptyString_ShouldThrowIllegalArgumentsException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> AssertUtils.notEmpty("", "string must not be null nor empty"));
  }

  @Test
  public void notEmpty_WithNotEmptyString_NoExceptionThrown() {

    assertThatCode(() -> AssertUtils.notEmpty("  ", "string must not be null nor empty"))
        .doesNotThrowAnyException();
  }

  @Test
  public void notEmpty_WithNullCollection_ShouldThrowNullPointerException() {

    assertThatNullPointerException()
        .isThrownBy(
            () ->
                AssertUtils.notEmpty(
                    (Collection<?>) null, "collection must not be null nor empty"));
  }

  @Test
  public void notEmpty_WithEmptyCollection_ShouldThrowIllegalArgumentsException() {

    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> AssertUtils.notEmpty(new ArrayList<>(), "collection must not be null nor empty"));
  }

  @Test
  public void notEmpty_WithNotEmptyCollection_NoExceptionThrown() {

    assertThatCode(
            () ->
                AssertUtils.notEmpty(
                    Arrays.stream(new Object[] {""}).collect(Collectors.toList()),
                    "collection must not be null nor empty"))
        .doesNotThrowAnyException();
  }

  @Test
  public void notEmpty_WithNullArray_ShouldThrowNullPointerException() {

    assertThatNullPointerException()
        .isThrownBy(
            () -> AssertUtils.notEmpty((Object[]) null, "array must not be null nor empty"));
  }

  @Test
  public void notEmpty_WithEmptyArray_ShouldThrowIllegalArgumentsException() {

    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> AssertUtils.notEmpty(new Object[] {}, "array must not be null nor empty"));
  }

  @Test
  public void notEmpty_WithNotEmptyArray_NoExceptionThrown() {

    assertThatCode(
            () -> AssertUtils.notEmpty(new Object[] {""}, "array must not be null nor empty"))
        .doesNotThrowAnyException();
  }

  @Test
  public void notNull_WithNullObject_ShouldThrowNullPointerException() {

    assertThatNullPointerException()
        .isThrownBy(() -> AssertUtils.notNull(null, "object must not be null"));
  }

  @Test
  public void notNull_WithNotNullObject_NoExceptionThrown() {

    assertThatCode(() -> AssertUtils.notNull(new Object(), "object must not be null"))
        .doesNotThrowAnyException();
  }
}
