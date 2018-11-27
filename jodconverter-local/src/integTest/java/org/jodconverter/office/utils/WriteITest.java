/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

package org.jodconverter.office.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sun.star.lang.XComponent;

import org.jodconverter.AbstractOfficeITest;
import org.jodconverter.LocalConverter;
import org.jodconverter.filter.Filter;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.OfficeContext;

public class WriteITest extends AbstractOfficeITest {

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void isTextAndGetTextDoc_withTextDocument_NoExceptionThrown() {

    final Filter filter =
        new Filter() {
          @Override
          public void doFilter(
              final OfficeContext context, final XComponent document, final FilterChain chain)
              throws Exception {

            assertThat(Write.isText(document)).isTrue();
            assertThat(Write.getTextDoc(null)).isNull();
            assertThat(Write.getTextDoc(document)).isNotNull();
          }
        };

    final File outputFile = new File(testFolder.getRoot(), "out.pdf");
    FileUtils.deleteQuietly(outputFile);

    assertThatCode(
            () -> {
              LocalConverter.builder()
                  .filterChain(filter)
                  .build()
                  .convert(new File(DOCUMENTS_DIR + "test.odt"))
                  .to(outputFile)
                  .execute();
            })
        .doesNotThrowAnyException();
  }

  @Test
  public void isNotTextAndGetTextDoc_withTextDocument_NoExceptionThrown() {

    final Filter filter =
        new Filter() {
          @Override
          public void doFilter(
              final OfficeContext context, final XComponent document, final FilterChain chain)
              throws Exception {

            assertThat(Write.isText(document)).isFalse();
            assertThat(Write.getTextDoc(null)).isNull();
            assertThat(Write.getTextDoc(document)).isNull();
          }
        };

    final File outputFile = new File(testFolder.getRoot(), "out.pdf");
    FileUtils.deleteQuietly(outputFile);

    assertThatCode(
            () -> {
              LocalConverter.builder()
                  .filterChain(filter)
                  .build()
                  .convert(new File(DOCUMENTS_DIR + "test.ods"))
                  .to(outputFile)
                  .execute();
            })
        .doesNotThrowAnyException();
  }
}
