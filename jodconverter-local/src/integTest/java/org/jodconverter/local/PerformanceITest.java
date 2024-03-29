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

package org.jodconverter.local;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.util.FileUtils;

/** Contains performance tests. */
@ExtendWith(LocalOfficeManagerExtension.class)
class PerformanceITest {

  private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceITest.class);

  private static final int MAX_CONVERSIONS = 10;
  private static final DocumentFormat INPUT_FORMAT = DefaultDocumentFormatRegistry.ODT;
  private static final DocumentFormat OUTPUT_FORMAT = DefaultDocumentFormatRegistry.PDF;

  private static String getDurationBreakdown(final long millis) {

    if (millis < 0) {
      throw new IllegalArgumentException("Duration must be greater than zero!");
    }

    long localMillis = millis;
    final long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
    localMillis -= TimeUnit.MINUTES.toMillis(minutes);
    final long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
    localMillis -= TimeUnit.SECONDS.toMillis(seconds);

    return String.format("%d min, %d sec, %d millisec", minutes, seconds, localMillis);
  }

  private void convertFileXTimes(final DocumentConverter converter, final File inputFile)
      throws IOException, OfficeException {

    final String baseName = FileUtils.getBaseName(inputFile.getName());

    final long start = System.currentTimeMillis();
    long split = start;
    for (int i = 0; i < MAX_CONVERSIONS; i++) {
      final File outputFile = File.createTempFile("test", "." + OUTPUT_FORMAT.getExtension());
      outputFile.deleteOnExit();

      LOGGER.info(
          "{} -- Converting {} to {}",
          baseName,
          PerformanceITest.INPUT_FORMAT.getExtension(),
          OUTPUT_FORMAT.getExtension());
      converter.convert(inputFile).to(outputFile).as(OUTPUT_FORMAT).execute();

      final long now = System.currentTimeMillis();
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("{} -- Conversion done in {} millisec.", baseName, now - split);
      }
      split = now;
    }

    final long total = System.currentTimeMillis() - start;
    LOGGER.info(
        "{} -- All {} conversions done in {}. The average per document is {} ms.",
        baseName,
        MAX_CONVERSIONS,
        getDurationBreakdown(total),
        total / MAX_CONVERSIONS);
  }

  @Test
  void runTest(final DocumentConverter converter) {

    final File dir = new File("src/integTest/resources/performance");
    final File[] files =
        dir.listFiles((dir1, name) -> name.toLowerCase(Locale.ROOT).endsWith(".odt"));

    assert files != null;
    assertThatCode(
            () -> {
              for (final File inputFile : files) {
                convertFileXTimes(converter, inputFile);
              }
            })
        .doesNotThrowAnyException();
  }
}
