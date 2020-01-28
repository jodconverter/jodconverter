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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalOfficeManagerExtension;
import org.jodconverter.local.filter.Filter;

@ExtendWith(LocalOfficeManagerExtension.class)
public class WriteITest {

  @Test
  public void isTextAndGetTextDoc_WithTextDocument_NoExceptionThrown(
      @TempDir File testFolder, OfficeManager manager) {

    final Filter filter =
        (context, document, chain) -> {
          assertThat(Write.isText(document)).isTrue();
          assertThat(Write.getTextDoc(null)).isNull();
          assertThat(Write.getTextDoc(document)).isNotNull();
        };

    final File sourceFile = documentFile("test.odt");
    final File outputFile = new File(testFolder, "out.pdf");
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(filter)
                    .build()
                    .convert(sourceFile)
                    .to(outputFile)
                    .execute())
        .doesNotThrowAnyException();
  }

  @Test
  public void isNotTextAndGetTextDoc_WithCalcDocument_NoExceptionThrown(
      @TempDir File testFolder, OfficeManager manager) {

    final Filter filter =
        (context, document, chain) -> {
          assertThat(Write.isText(document)).isFalse();
          assertThat(Write.getTextDoc(null)).isNull();
          assertThat(Write.getTextDoc(document)).isNull();
        };

    final File sourceFile = documentFile("test.ods");
    final File outputFile = new File(testFolder, "out.pdf");
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(filter)
                    .build()
                    .convert(sourceFile)
                    .to(outputFile)
                    .execute())
        .doesNotThrowAnyException();
  }
}
