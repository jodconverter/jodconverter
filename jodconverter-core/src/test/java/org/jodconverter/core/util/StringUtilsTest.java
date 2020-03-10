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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link StringUtils} class. */
public class StringUtilsTest {

  @Test
  public void new_ClassWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(StringUtils.class);
  }

  @Test
  public void appendIfMissing_WithNullInputString_ShouldReturnNull() {
    assertThat(StringUtils.appendIfMissing(null, "/")).isNull();
  }

  @Test
  public void appendIfMissing_WithNullSuffix_ShouldReturnInputString() {
    assertThat(StringUtils.appendIfMissing("input", null)).isEqualTo("input");
  }

  @Test
  public void appendIfMissing_WithEmptySuffix_ShouldReturnInputString() {
    assertThat(StringUtils.appendIfMissing("input", "")).isEqualTo("input");
  }

  @Test
  public void appendIfMissing_WithNotMissingSuffix_ShouldReturnInputString() {
    assertThat(StringUtils.appendIfMissing("path/to/dir/", "/")).isEqualTo("path/to/dir/");
  }

  @Test
  public void appendIfMissing_WithMissingSuffix_ShouldReturnInputStringWithSuffix() {
    assertThat(StringUtils.appendIfMissing("path/to/dir", "/")).isEqualTo("path/to/dir/");
  }

  @Test
  public void endsWithAny_WithNullString_ShouldReturnFalse() {
    assertThat(StringUtils.endsWithAny(null)).isFalse();
  }

  @Test
  public void endsWithAny_WithEmptyString_ShouldReturnFalse() {
    assertThat(StringUtils.endsWithAny("")).isFalse();
  }

  @Test
  public void endsWithAny_WithEmptySearchStrings_ShouldReturnFalse() {
    assertThat(StringUtils.endsWithAny("input")).isFalse();
  }

  @Test
  public void endsWithAny_WithNullOrNotEndsWithSearchStrings_ShouldReturnFalse() {
    assertThat(StringUtils.endsWithAny("input", "test1", null, "test2")).isFalse();
  }

  @Test
  public void endsWithAny_WithEndsWithSearchStrings_ShouldReturnTrue() {
    assertThat(StringUtils.endsWithAny("input", "test1", null, "ut")).isTrue();
  }

  @Test
  public void isEmpty_WithNullString_ShouldReturnTrue() {
    assertThat(StringUtils.isEmpty(null)).isTrue();
  }

  @Test
  public void isEmpty_WithEmptyString_ShouldReturnTrue() {
    assertThat(StringUtils.isEmpty("")).isTrue();
  }

  @Test
  public void isEmpty_WithBlankString_ShouldReturnFalse() {
    assertThat(StringUtils.isEmpty(" ")).isFalse();
  }

  @Test
  public void isEmpty_WithNotEmptyString_ShouldReturnFalse() {
    assertThat(StringUtils.isEmpty("test")).isFalse();
  }

  @Test
  public void isNotEmpty_WithNullString_ShouldReturnFalse() {
    assertThat(StringUtils.isNotEmpty(null)).isFalse();
  }

  @Test
  public void isNotEmpty_WithEmptyString_ShouldReturnFalse() {
    assertThat(StringUtils.isNotEmpty("")).isFalse();
  }

  @Test
  public void isNotEmpty_WithBlankString_ShouldReturnTrue() {
    assertThat(StringUtils.isNotEmpty(" ")).isTrue();
  }

  @Test
  public void isNotEmpty_WithNotEmptyString_ShouldReturnTrue() {
    assertThat(StringUtils.isNotEmpty("test")).isTrue();
  }

  @Test
  public void isBlank_WithNullString_ShouldReturnTrue() {
    assertThat(StringUtils.isBlank(null)).isTrue();
  }

  @Test
  public void isBlank_WithEmptyString_ShouldReturnTrue() {
    assertThat(StringUtils.isBlank("")).isTrue();
  }

  @Test
  public void isBlank_WithBlankString_ShouldReturnTrue() {
    assertThat(StringUtils.isBlank("  \t  ")).isTrue();
  }

  @Test
  public void isBlank_WithNotBlankString_ShouldReturnFalse() {
    assertThat(StringUtils.isBlank("  test\t  ")).isFalse();
  }

  @Test
  public void isNotBlank_WithNullString_ShouldReturnFalse() {
    assertThat(StringUtils.isNotBlank(null)).isFalse();
  }

  @Test
  public void isNotBlank_WithEmptyString_ShouldReturnFalse() {
    assertThat(StringUtils.isNotBlank("")).isFalse();
  }

  @Test
  public void isNotBlank_WithBlankString_ShouldReturnFalse() {
    assertThat(StringUtils.isNotBlank("  \t  ")).isFalse();
  }

  @Test
  public void isNotBlank_WithNotBlankString_ShouldReturnTrue() {
    assertThat(StringUtils.isNotBlank("  test\t  ")).isTrue();
  }
}
