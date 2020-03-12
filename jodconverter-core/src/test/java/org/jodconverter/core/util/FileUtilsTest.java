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

/** Contains tests for the {@link FileUtils} class. */
public class FileUtilsTest {

  @Test
  public void new_ClassWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(FileUtils.class);
  }

  @Test
  public void getName_WithNull_ShouldReturnNull() {
    assertThat(FileUtils.getName(null)).isNull();
  }

  @Test
  public void getName_WithFullPath_ShouldReturnFileName() {
    assertThat(FileUtils.getName("a/b/c.txt")).isEqualTo("c.txt");
  }

  @Test
  public void getName_WithOnlyFileName_ShouldReturnFileName() {
    assertThat(FileUtils.getName("c.txt")).isEqualTo("c.txt");
  }

  @Test
  public void getName_WithFullPathWithoutExtension_ShouldReturnFileName() {
    assertThat(FileUtils.getName("a/b/c")).isEqualTo("c");
  }

  @Test
  public void getName_WithOnlyFileNameWithoutExtension_ShouldReturnFileName() {
    assertThat(FileUtils.getName("c")).isEqualTo("c");
  }

  @Test
  public void getName_WithFullDirectoryPath_ShouldReturnEmptyString() {
    assertThat(FileUtils.getName("a/b/")).isEqualTo("");
  }

  @Test
  public void getName_WithSlash_ShouldReturnEmptyString() {
    assertThat(FileUtils.getName("/")).isEqualTo("");
  }

  @Test
  public void getBaseName_WithNull_ShouldReturnNull() {
    assertThat(FileUtils.getBaseName(null)).isNull();
  }

  @Test
  public void getBaseName_WithFullPath_ShouldReturnBaseName() {
    assertThat(FileUtils.getBaseName("a/b/c.txt")).isEqualTo("c");
  }

  @Test
  public void getBaseName_WithOnlyFileName_ShouldReturnBaseName() {
    assertThat(FileUtils.getBaseName("c.txt")).isEqualTo("c");
  }

  @Test
  public void getBaseName_WithFullPathWithoutExtension_ShouldReturnFileName() {
    assertThat(FileUtils.getBaseName("a/b/c")).isEqualTo("c");
  }

  @Test
  public void getBaseName_WithOnlyFileNameWithoutExtension_ShouldReturnFileName() {
    assertThat(FileUtils.getBaseName("c")).isEqualTo("c");
  }

  @Test
  public void getBaseName_WithFullDirectoryPath_ShouldReturnEmptyString() {
    assertThat(FileUtils.getBaseName("a/b/")).isEqualTo("");
  }

  @Test
  public void getBaseName_WithSlash_ShouldReturnEmptyString() {
    assertThat(FileUtils.getBaseName("/")).isEqualTo("");
  }

  @Test
  public void getExtension_WithNull_ShouldReturnNull() {
    assertThat(FileUtils.getExtension(null)).isNull();
  }

  @Test
  public void getExtension_WithFullPath_ShouldReturnExtension() {
    assertThat(FileUtils.getExtension("a/b/c.txt")).isEqualTo("txt");
  }

  @Test
  public void getExtension_WithOnlyFileName_ShouldReturnExtension() {
    assertThat(FileUtils.getExtension("c.txt")).isEqualTo("txt");
  }

  @Test
  public void getExtension_WithFullPathWithoutExtension_ShouldReturnEmptyString() {
    assertThat(FileUtils.getExtension("a/b/c")).isEqualTo("");
  }

  @Test
  public void getExtension_WithOnlyFileNameWithoutExtension_ShouldReturnEmptyString() {
    assertThat(FileUtils.getExtension("c")).isEqualTo("");
  }

  @Test
  public void getExtension_WithFullDirectoryPath_ShouldReturnEmptyString() {
    assertThat(FileUtils.getExtension("a/b/")).isEqualTo("");
  }

  @Test
  public void getExtension_WithSlash_ShouldReturnEmptyString() {
    assertThat(FileUtils.getExtension("/")).isEqualTo("");
  }

  @Test
  public void getExtension_WithDot_ShouldReturnEmptyString() {
    assertThat(FileUtils.getExtension(".")).isEqualTo("");
  }

  @Test
  public void getFullPath_WithNull_ShouldReturnNull() {
    assertThat(FileUtils.getBaseName(null)).isNull();
  }
}
