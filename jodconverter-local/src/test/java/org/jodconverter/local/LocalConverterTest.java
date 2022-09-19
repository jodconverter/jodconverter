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

package org.jodconverter.local;

import static org.assertj.core.api.Assertions.*;
import static org.jodconverter.local.ResourceUtil.documentFile;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import com.sun.star.document.UpdateDocMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.SimpleDocumentFormatRegistry;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.filter.DefaultFilterChain;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.task.LocalConversionTask;

/** Contains tests for the {@link LocalConverter} class. */
class LocalConverterTest {

  private static final File SOURCE_FILE = documentFile("test.txt");

  private OfficeManager officeManager;

  @BeforeEach
  void setUp() {
    officeManager = mock(OfficeManager.class);
  }

  @Nested
  class Make {

    @Test
    void withOfficeManagerInstalled_ShouldSuccess(final @TempDir File testFolder) {

      final OfficeManager manager = InstalledOfficeManagerHolder.getInstance();
      InstalledOfficeManagerHolder.setInstance(officeManager);

      final File targetFile = new File(testFolder, "test.pdf");
      try {
        assertThatCode(() -> LocalConverter.make().convert(SOURCE_FILE).to(targetFile).execute())
            .doesNotThrowAnyException();
      } finally {
        InstalledOfficeManagerHolder.setInstance(manager);
      }
    }

    @Test
    void withoutOfficeManagerInstalled_ShouldThrowIllegalStateException(
        final @TempDir File testFolder) {

      final OfficeManager manager = InstalledOfficeManagerHolder.getInstance();
      InstalledOfficeManagerHolder.setInstance(null);

      final File targetFile = new File(testFolder, "test.pdf");
      try {
        assertThatIllegalStateException()
            .isThrownBy(() -> LocalConverter.make().convert(SOURCE_FILE).to(targetFile).execute());
      } finally {
        InstalledOfficeManagerHolder.setInstance(manager);
      }
    }
  }

  @Nested
  class Builder {

    @Test
    void withCustomFormatRegistry_ShouldUseCustomRegistry() {

      final SimpleDocumentFormatRegistry registry = new SimpleDocumentFormatRegistry();
      registry.addFormat(DefaultDocumentFormatRegistry.DOC);
      registry.addFormat(DefaultDocumentFormatRegistry.PDF);
      final LocalConverter manager =
          LocalConverter.builder().officeManager(officeManager).formatRegistry(registry).build();

      assertThat(manager)
          .extracting("formatRegistry")
          .isInstanceOfSatisfying(
              SimpleDocumentFormatRegistry.class,
              simpleDocumentFormatRegistry -> {
                assertThat(simpleDocumentFormatRegistry.getFormatByExtension("txt")).isNull();
                assertThat(simpleDocumentFormatRegistry.getFormatByExtension("doc"))
                    .usingRecursiveComparison()
                    .isEqualTo(DefaultDocumentFormatRegistry.DOC);
                assertThat(simpleDocumentFormatRegistry.getFormatByExtension("pdf"))
                    .usingRecursiveComparison()
                    .isEqualTo(DefaultDocumentFormatRegistry.PDF);
              });
    }

    @Test
    void withNullFilters_ShouldThrowNullPointerException(final @TempDir File testFolder) {

      final File targetFile = new File(testFolder, "test.pdf");

      assertThatNullPointerException()
          .isThrownBy(
              () ->
                  LocalConverter.builder()
                      .officeManager(officeManager)
                      .filterChain((Filter[]) null)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile)
                      .execute());
    }

    @Test
    void withEmptyFilter_ShouldThrowIllegalArgumentException(final @TempDir File testFolder) {

      final File targetFile = new File(testFolder, "test.pdf");

      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  LocalConverter.builder()
                      .officeManager(officeManager)
                      .filterChain(new Filter[0])
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile)
                      .execute());
    }

    @Test
    void withNullFilterChain_ShouldThrowNullPointerException(final @TempDir File testFolder) {

      final File targetFile = new File(testFolder, "test.pdf");

      assertThatNullPointerException()
          .isThrownBy(
              () ->
                  LocalConverter.builder()
                      .officeManager(officeManager)
                      .filterChain((FilterChain) null)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile)
                      .execute());
    }
  }

  @Nested
  class Convert {

    @Test
    void withFilters_ShouldCreateConverterWithExpectedFilters(final @TempDir File testFolder)
        throws Exception {

      final Filter filter = mock(Filter.class);
      final File targetFile = new File(testFolder, "test.pdf");

      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(officeManager)
                      .filterChain(filter)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile)
                      .execute())
          .doesNotThrowAnyException();
      final ArgumentCaptor<LocalConversionTask> arg =
          ArgumentCaptor.forClass(LocalConversionTask.class);
      verify(officeManager, times(1)).execute(arg.capture());
      assertThat(arg.getValue())
          .extracting("filterChain.filters")
          .asList()
          .satisfies(filters -> assertThat(filters.get(0)).isEqualTo(filter));
    }

    @Test
    void withFilterChain_ShouldCreateConverterWithExpectedFilters(final @TempDir File testFolder)
        throws Exception {

      final Filter filter = mock(Filter.class);
      final FilterChain chain = new DefaultFilterChain(filter);
      final File targetFile = new File(testFolder, "test.pdf");

      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(officeManager)
                      .filterChain(chain)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile)
                      .execute())
          .doesNotThrowAnyException();
      final ArgumentCaptor<LocalConversionTask> arg =
          ArgumentCaptor.forClass(LocalConversionTask.class);
      verify(officeManager, times(1)).execute(arg.capture());
      assertThat(arg.getValue())
          .extracting("filterChain.filters")
          .asList()
          .satisfies(filters -> assertThat(filters.get(0)).isEqualTo(filter));
    }

    @Test
    void withCustomLoadProperties_ShouldCreateConverterWithExpectedLoadProperties(
        final @TempDir File testFolder) throws OfficeException {

      final Map<String, Object> loadProperties = new HashMap<>();
      loadProperties.put("ReadOnly", true);
      loadProperties.put("UpdateDocMode", UpdateDocMode.ACCORDING_TO_CONFIG);

      final Map<String, Object> expectedProperties = new HashMap<>();
      expectedProperties.put("Hidden", true);
      expectedProperties.put("ReadOnly", true);
      expectedProperties.put("UpdateDocMode", UpdateDocMode.ACCORDING_TO_CONFIG);

      final File targetFile = new File(testFolder, "test.pdf");

      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(officeManager)
                      .loadProperties(loadProperties)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile)
                      .execute())
          .doesNotThrowAnyException();

      // Verify that the office manager has executed a task with the expected properties.
      final ArgumentCaptor<LocalConversionTask> arg =
          ArgumentCaptor.forClass(LocalConversionTask.class);
      verify(officeManager, times(1)).execute(arg.capture());
      assertThat(arg.getValue()).extracting("loadProperties").isEqualTo(expectedProperties);
    }

    @Test
    void withCustomLoadProperty_ShouldCreateConverterWithExpectedLoadProperties(
        final @TempDir File testFolder) throws OfficeException {

      final Map<String, Object> expectedProperties =
          new HashMap<>(LocalConverter.DEFAULT_LOAD_PROPERTIES);
      expectedProperties.put("Hidden", false);
      expectedProperties.put("Password", "myPassword");

      final File targetFile = new File(testFolder, "test.pdf");

      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(officeManager)
                      .loadProperty("Hidden", false)
                      .loadProperty("Password", "myPassword")
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile)
                      .execute())
          .doesNotThrowAnyException();

      // Verify that the office manager has executed a task with the expected properties.
      final ArgumentCaptor<LocalConversionTask> arg =
          ArgumentCaptor.forClass(LocalConversionTask.class);
      verify(officeManager, times(1)).execute(arg.capture());
      assertThat(arg.getValue()).extracting("loadProperties").isEqualTo(expectedProperties);
    }

    @Test
    void withCustomLoadPropertiesWithoutDefault_ShouldCreateConverterWithExpectedLoadProperties(
        final @TempDir File testFolder) throws OfficeException {

      final Map<String, Object> loadProperties = new HashMap<>();
      loadProperties.put("Hidden", false);

      final File targetFile = new File(testFolder, "test.pdf");

      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(officeManager)
                      .applyDefaultLoadProperties(false)
                      .loadProperties(loadProperties)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile)
                      .execute())
          .doesNotThrowAnyException();

      // Verify that the office manager has executed a task with the expected properties.
      final ArgumentCaptor<LocalConversionTask> arg =
          ArgumentCaptor.forClass(LocalConversionTask.class);
      verify(officeManager, times(1)).execute(arg.capture());
      assertThat(arg.getValue()).extracting("loadProperties").isEqualTo(loadProperties);
    }

    @Test
    void withCustomLoadPropertyWithoutDefault_ShouldCreateConverterWithExpectedLoadProperties(
        final @TempDir File testFolder) throws OfficeException {

      final Map<String, Object> expectedProperties = new HashMap<>();
      expectedProperties.put("Hidden", false);

      final File targetFile = new File(testFolder, "test.pdf");

      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(officeManager)
                      .applyDefaultLoadProperties(false)
                      .loadProperty("Hidden", false)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile)
                      .execute())
          .doesNotThrowAnyException();

      // Verify that the office manager has executed a task with the expected properties.
      final ArgumentCaptor<LocalConversionTask> arg =
          ArgumentCaptor.forClass(LocalConversionTask.class);
      verify(officeManager, times(1)).execute(arg.capture());
      assertThat(arg.getValue()).extracting("loadProperties").isEqualTo(expectedProperties);
    }

    @Test
    void withCustomStoreProperties_ShouldCreateConverterWithExpectedStoreProperties(
        final @TempDir File testFolder) throws OfficeException {

      final Map<String, Object> filterData = new HashMap<>();
      filterData.put("PageRange", "1");
      final Map<String, Object> storeProperties = new HashMap<>();
      storeProperties.put("FilterData", filterData);
      final File targetFile = new File(testFolder, "test.pdf");

      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(officeManager)
                      .storeProperties(storeProperties)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile)
                      .execute())
          .doesNotThrowAnyException();

      // Verify that the office manager has executed a task with the expected properties.
      final ArgumentCaptor<LocalConversionTask> arg =
          ArgumentCaptor.forClass(LocalConversionTask.class);
      verify(officeManager, times(1)).execute(arg.capture());
      assertThat(arg.getValue()).extracting("storeProperties").isEqualTo(storeProperties);
    }

    @Test
    void withCustomStoreProperty_ShouldCreateConverterWithExpectedStoreProperties(
        final @TempDir File testFolder) throws OfficeException {

      final Map<String, Object> filterData = new HashMap<>();
      filterData.put("PageRange", "1");
      final Map<String, Object> storeProperties = new HashMap<>();
      storeProperties.put("FilterData", filterData);
      final File targetFile = new File(testFolder, "test.pdf");

      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(officeManager)
                      .storeProperty("FilterData", filterData)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile)
                      .execute())
          .doesNotThrowAnyException();

      // Verify that the office manager has executed a task with the expected properties.
      final ArgumentCaptor<LocalConversionTask> arg =
          ArgumentCaptor.forClass(LocalConversionTask.class);
      verify(officeManager, times(1)).execute(arg.capture());
      assertThat(arg.getValue()).extracting("storeProperties").isEqualTo(storeProperties);
    }

    @Test
    void withNonTemporaryFileMaker_ShouldThrowIllegalStateExceptionForInputStream(
        final @TempDir File testFolder) {

      final File targetFile = new File(testFolder, "test.pdf");
      assertThatIllegalStateException()
          .isThrownBy(
              () -> {
                try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
                  LocalConverter.make(officeManager)
                      .convert(stream)
                      .as(DefaultDocumentFormatRegistry.TXT)
                      .to(targetFile)
                      .execute();
                }
              })
          .withMessageMatching(".*TemporaryFileMaker.*InputStream.*");
    }

    @Test
    void withNonTemporaryFileMaker_ShouldThrowIllegalStateExceptionForOutputStream(
        final @TempDir File testFolder) {

      final File targetFile = new File(testFolder, "test.pdf");
      assertThatIllegalStateException()
          .isThrownBy(
              () -> {
                try (OutputStream stream = Files.newOutputStream(targetFile.toPath())) {
                  LocalConverter.make(officeManager)
                      .convert(SOURCE_FILE)
                      .to(stream)
                      .as(DefaultDocumentFormatRegistry.PDF)
                      .execute();
                }
              })
          .withMessageMatching(".*TemporaryFileMaker.*OutputStream.*");
    }

    @Test
    void withoutUseUnsafeQuietMode_ShouldCreateConverterWithExpectedLoadProperties(
        final @TempDir File testFolder) throws OfficeException {

      final Map<String, Object> expectedProperties = new HashMap<>();
      expectedProperties.put("Hidden", true);
      expectedProperties.put("ReadOnly", true);
      expectedProperties.put("UpdateDocMode", UpdateDocMode.NO_UPDATE);

      final File targetFile = new File(testFolder, "test.pdf");

      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(officeManager)
                      .useUnsafeQuietUpdate(false)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile)
                      .execute())
          .doesNotThrowAnyException();

      // Verify that the office manager has executed a task with the expected properties.
      final ArgumentCaptor<LocalConversionTask> arg =
          ArgumentCaptor.forClass(LocalConversionTask.class);
      verify(officeManager, times(1)).execute(arg.capture());
      assertThat(arg.getValue()).extracting("loadProperties").isEqualTo(expectedProperties);
    }

    @Test
    void withUseUnsafeQuietMode_ShouldCreateConverterWithExpectedLoadProperties(
        final @TempDir File testFolder) throws OfficeException {

      final Map<String, Object> expectedProperties = new HashMap<>();
      expectedProperties.put("Hidden", true);
      expectedProperties.put("ReadOnly", true);
      expectedProperties.put("UpdateDocMode", UpdateDocMode.QUIET_UPDATE);

      final File targetFile = new File(testFolder, "test.pdf");

      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(officeManager)
                      .useUnsafeQuietUpdate(true)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(targetFile)
                      .execute())
          .doesNotThrowAnyException();

      // Verify that the office manager has executed a task with the expected properties.
      final ArgumentCaptor<LocalConversionTask> arg =
          ArgumentCaptor.forClass(LocalConversionTask.class);
      verify(officeManager, times(1)).execute(arg.capture());
      assertThat(arg.getValue()).extracting("loadProperties").isEqualTo(expectedProperties);
    }
  }
}
