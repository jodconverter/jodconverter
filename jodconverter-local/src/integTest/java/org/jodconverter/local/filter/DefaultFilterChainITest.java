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

package org.jodconverter.local.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.jodconverter.local.ResourceUtil.documentFile;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalOfficeManagerExtension;

/** Contains tests for the {@link DefaultFilterChain} class. */
@ExtendWith(LocalOfficeManagerExtension.class)
public class DefaultFilterChainITest {

  private static final String SOURCE_FILENAME = "test_multi_page.doc";
  private static final File SOURCE_FILE = documentFile(SOURCE_FILENAME);

  /** Test that resetting a chain will actually allow us to reuse it. */
  @Test
  public void reset_WithPageCounterAndSelector_ShoudCountProperSizesForBothUsage(
      final @TempDir File testFolder, final OfficeManager manager) throws IOException {

    final File targetFile1 = new File(testFolder, SOURCE_FILENAME + ".page1.txt");
    final File targetFile2 = new File(testFolder, SOURCE_FILENAME + ".page1again.txt");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filters
    final DefaultFilterChain chain =
        new DefaultFilterChain(
            count1, new PagesSelectorFilter(1), new RefreshFilter(false), count2);
    final LocalConverter converter =
        LocalConverter.builder().officeManager(manager).filterChain(chain).build();

    assertThatCode(() -> converter.convert(SOURCE_FILE).to(targetFile1).execute())
        .doesNotThrowAnyException();

    final String content = FileUtils.readFileToString(targetFile1, StandardCharsets.UTF_8);
    assertThat(content)
        .as("Check content: %s", content)
        .contains("Test document Page 1")
        .doesNotContain("Test document Page 2")
        .doesNotContain("Test document Page 3");
    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(1);

    // Reset the chain and test the filters again
    chain.reset();
    assertThatCode(() -> converter.convert(targetFile1).to(targetFile2).execute())
        .doesNotThrowAnyException();
    assertThat(count1.getPageCount()).isEqualTo(1);
    assertThat(count2.getPageCount()).isEqualTo(1);
  }

  /** Test that setting off the automatic insertion of refresh filter won't execute any refresh. */
  @Test
  public void reset_WithEndsWithRefreshFilterOff_ShoudNotApplyRefreshFilter(
      final @TempDir File testFolder, final OfficeManager manager) throws Exception {

    // Replace the LAST_REFRESH singleton

    // TODO: Find a way to test under jdk12/13
    // This is not supported with jdk 12/13
    // https://bugs.openjdk.java.net/browse/JDK-8217225
    final RefreshFilter oldFilter = RefreshFilter.LAST_REFRESH;
    final RefreshFilter newFilter = mock(RefreshFilter.class);
    final boolean restoreFilter =
        setFinalStatic(RefreshFilter.class.getDeclaredField("LAST_REFRESH"), newFilter);
    assumeTrue(restoreFilter, () -> "Aborting test: could not set mock static field");

    // Then execute the test
    try {
      final File targetFile1 = new File(testFolder, SOURCE_FILENAME + ".page1.txt");
      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(manager)
                      .filterChain(
                          new DefaultFilterChain(
                              false, new PageCounterFilter(), new PagesSelectorFilter(1)))
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile1)
                      .execute())
          .doesNotThrowAnyException();

      // Verify that the
      verify(newFilter, times(0)).doFilter(any(), any(), any());
    } finally {
      setFinalStatic(RefreshFilter.class.getDeclaredField("LAST_REFRESH"), oldFilter);
    }
  }

  /** Test that setting on the automatic insertion of refresh filter will execute it. */
  @Test
  public void reset_WithEndsWithRefreshFilterOn_ShoudApplyRefreshFilter(
      final @TempDir File testFolder, final OfficeManager manager) throws Exception {

    // Replace the LAST_REFRESH singleton

    // TODO: Find a way to test under jdk12/13
    // This is not supported with jdk 12/13
    // https://bugs.openjdk.java.net/browse/JDK-8217225
    final RefreshFilter oldFilter = RefreshFilter.LAST_REFRESH;
    final RefreshFilter newFilter = mock(RefreshFilter.class);
    final boolean restoreFilter =
        setFinalStatic(RefreshFilter.class.getDeclaredField("LAST_REFRESH"), newFilter);
    assumeTrue(restoreFilter, () -> "Aborting test: could not set mock static field");

    // Then execute the test
    try {
      final File targetFile1 = new File(testFolder, SOURCE_FILENAME + ".page1.txt");
      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(manager)
                      .filterChain(
                          new DefaultFilterChain(
                              new PageCounterFilter(), new PagesSelectorFilter(1)))
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile1)
                      .execute())
          .doesNotThrowAnyException();

      // Verify that the
      verify(newFilter, times(1)).doFilter(any(), any(), any());
    } finally {
      setFinalStatic(RefreshFilter.class.getDeclaredField("LAST_REFRESH"), oldFilter);
    }
  }

  private static boolean setFinalStatic(final Field field, final Object newValue) {

    try {
      field.setAccessible(true);
      // This is not supported with jdk 12/13
      // https://bugs.openjdk.java.net/browse/JDK-8217225
      final Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
      field.set(null, newValue);
      return true;
    } catch (Exception ex) {
      LoggerFactory.getLogger(DefaultFilterChainITest.class)
          .warn(
              "Enable to set final static field: {}.{}",
              field.getDeclaringClass().getSimpleName(),
              field.getName());
    }
    return false;
  }
}
