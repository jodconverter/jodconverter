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

import java.io.File;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.local.ConvertUtil.ConvertRunner;
import org.jodconverter.local.office.LocalOfficeManager;

/** Contain a stress test. */
class StressITest {

  private static final Logger LOGGER = LoggerFactory.getLogger(StressITest.class);

  // private static final int MAX_CONVERSIONS = 10;
  private static final int MAX_CONVERSIONS = 1024;
  private static final int MAX_THREADS = 128;
  private static final int MAX_PROCESS_TASKS = 10;

  private static final DocumentFormat INPUT_FORMAT =
      DefaultDocumentFormatRegistry.getInstance().getFormatByExtension("rtf");
  private static final DocumentFormat OUTPUT_FORMAT =
      DefaultDocumentFormatRegistry.getInstance().getFormatByExtension("pdf");

  /**
   * This test will run multiple parallel conversions, using 8 office processes. Just change the
   * MAX_* constants to control the numbers of conversion, threads and maximum conversion per office
   * process allowed.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  void runParallelConversions(final @TempDir File testFolder) throws Exception {

    // Log at DEBUG level, to the console and to a log file (build/integTest-results/test.log)
    // to be able to see if an error occurred.
    final var context = (LoggerContext) LogManager.getContext(false);
    final var defaultConfig = context.getConfigLocation();
    context.setConfigLocation(
        Objects.requireNonNull(getClass().getResource("/log4j2-stress.xml")).toURI());

    // Configure the office manager in a way that maximizes possible race conditions.
    final var officeManager =
        LocalOfficeManager.builder()
            .portNumbers(2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009)
            // .portNumbers(2002, 2003)
            .maxTasksPerProcess(MAX_PROCESS_TASKS)
            .build();
    final var converter = LocalConverter.make(officeManager);

    officeManager.start();
    try {
      final var source =
          new File(
              "src/integTest/resources/documents/test."
                  + Objects.requireNonNull(INPUT_FORMAT).getExtension());

      final var threads = new Thread[MAX_THREADS];

      var first = true;
      var threadCount = 0;

      for (var i = 0; i < MAX_CONVERSIONS; i++) {
        final var target =
            new File(
                testFolder,
                "test_" + i + "." + Objects.requireNonNull(OUTPUT_FORMAT).getExtension());
        target.deleteOnExit();

        // Converts the first document without threads to ensure everything is OK.
        if (first) {
          converter.convert(source).to(target).execute();
          first = false;
        }

        LOGGER.info("Creating thread {}", threadCount);
        final var runnable = new ConvertRunner(source, target, converter);
        threads[threadCount] = new Thread(runnable);
        threads[threadCount++].start();

        if (threadCount == MAX_THREADS) {
          for (var j = 0; j < threadCount; j++) {
            threads[j].join();
          }
          threadCount = 0;
        }
      }

      // Wait for remaining threads.
      for (var j = 0; j < threadCount; j++) {
        threads[j].join();
      }

    } finally {
      officeManager.stop();
      context.setConfigLocation(defaultConfig);
    }
  }
}
