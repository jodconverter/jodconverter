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

package org.jodconverter.filter.draw;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.AbstractOfficeITest;
import org.jodconverter.LocalConverter;
import org.jodconverter.filter.PageCounterFilter;

public class PageSelectorFilterITest extends AbstractOfficeITest {

  private static final String SOURCE_FILENAME = "test_multi_page.odg";
  private static final File SOURCE_FILE = new File(DOCUMENTS_DIR, SOURCE_FILENAME);

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void doFilter_SelectPage2_ShouldConvertOnlyPage2() throws Exception {

    final File targetFile = new File(testFolder.getRoot(), SOURCE_FILENAME + ".page2.odg");

    // Create the PageSelectorFilter to test.
    final PageSelectorFilter selectorFilter = new PageSelectorFilter(2);
    final PageCounterFilter countFilter = new PageCounterFilter();

    // Test the filter

    LocalConverter.builder()
        .filterChain(selectorFilter, countFilter)
        .build()
        .convert(SOURCE_FILE)
        .to(targetFile)
        .execute();

    assertThat(countFilter.getPageCount()).isEqualTo(1);
  }
}
