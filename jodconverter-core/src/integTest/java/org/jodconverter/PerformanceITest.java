/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

package org.jodconverter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;

@SuppressWarnings("PMD.LawOfDemeter")
public class PerformanceITest extends BaseOfficeITest {

  private static final Logger logger = LoggerFactory.getLogger(PerformanceITest.class);

  private static final int MAX_CONVERSIONS = 10;
  private static final String INPUT_EXTENSION = "odt";
  private static final String OUTPUT_EXTENSION = "pdf";

  private static String getDurationBreakdown(final long millis) {

    if (millis < 0) {
      throw new IllegalArgumentException("Duration must be greater than zero!");
    }

    long ms = millis;
    final long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
    ms -= TimeUnit.MINUTES.toMillis(minutes);
    final long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
    ms -= TimeUnit.SECONDS.toMillis(seconds);

    return String.format("%d min, %d sec, %d millisec", minutes, seconds, ms);
  }

  private void convertFileXTimes(
      final File inputFile, final DocumentFormat inputFormat, final DocumentFormat outputFormat)
      throws IOException, OfficeException {

    final String baseName = FilenameUtils.getBaseName(inputFile.getName());

    long lastSplitTime = 0L;
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    for (int i = 0; i < MAX_CONVERSIONS; i++) {
      final File outputFile = File.createTempFile("test", "." + outputFormat.getExtension());
      outputFile.deleteOnExit();

      logger.info(
          baseName
              + " -- converting "
              + inputFormat.getExtension()
              + " to "
              + outputFormat.getExtension()
              + "... ");
      converter.convert(inputFile).as(inputFormat).to(outputFile).as(outputFormat).execute();

      stopWatch.split();
      final long splitTime = stopWatch.getSplitTime();
      logger.info(baseName + "-- conversion done in " + (splitTime - lastSplitTime) + " millisec.");
      lastSplitTime = splitTime;
    }
    stopWatch.stop();
    final long totalConversionTime = stopWatch.getTime();

    logger.info(
        baseName
            + "-- all "
            + MAX_CONVERSIONS
            + " conversions done in "
            + getDurationBreakdown(totalConversionTime)
            + ". The average per document is "
            + (totalConversionTime / MAX_CONVERSIONS)
            + " ms.");
  }

  @Test
  public void runTest() throws Exception {

    final DocumentFormat inputFormat = formatRegistry.getFormatByExtension(INPUT_EXTENSION);
    final DocumentFormat outputFormat = formatRegistry.getFormatByExtension(OUTPUT_EXTENSION);

    final File dir = new File("src/integTest/resources/performance");
    final File[] files =
        dir.listFiles((FileFilter) new WildcardFileFilter("*.odt", IOCase.INSENSITIVE));

    for (final File inputFile : files) {

      convertFileXTimes(inputFile, inputFormat, outputFormat);
    }
  }
}
