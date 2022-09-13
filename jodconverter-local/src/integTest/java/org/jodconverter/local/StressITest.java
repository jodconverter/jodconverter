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

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Objects;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.local.ConvertUtil.ConvertRunner;
import org.jodconverter.local.office.LocalOfficeManager;

/** Contain a stress test. */
class StressITest {

  private static final Logger LOGGER = Logger.getLogger(StressITest.class);

  // private static final int MAX_CONVERSIONS = 10;
  private static final int MAX_CONVERSIONS = 1024;
  private static final int MAX_THREADS = 128;
  private static final int MAX_PROCESS_TASKS = 10;

  private static final DocumentFormat INPUT_FORMAT =
      DefaultDocumentFormatRegistry.getFormatByExtension("rtf");
  private static final DocumentFormat OUTPUT_FORMAT =
      DefaultDocumentFormatRegistry.getFormatByExtension("pdf");

  private static final String PATTERN = "%d %-5p --- [%t] %c{1.} - %m%n";

  /**
   * This test will run multiple parallel conversions, using 8 office processes. Just change the
   * MAX_* constants to control the numbers of conversion, threads and maximum conversion per office
   * process allowed.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  void runParallelConversions(final @TempDir File testFolder) throws Exception {

    final File logFile = new File("build/integTest-results/test.log");
    FileUtils.deleteQuietly(logFile);

    // Create console appender
    final ConsoleAppender console = new ConsoleAppender();
    console.setWriter(new OutputStreamWriter(System.out));
    console.setLayout(new PatternLayout(PATTERN));
    console.setThreshold(Level.DEBUG);
    console.activateOptions();
    Logger.getRootLogger().addAppender(console);

    // Keep a log file to be able to see if an error occurred
    final FileAppender fileAppender = new FileAppender();
    fileAppender.setName("FileLogger");
    fileAppender.setFile(logFile.getPath());
    fileAppender.setLayout(new PatternLayout(PATTERN));
    fileAppender.setThreshold(Level.DEBUG);
    fileAppender.setAppend(true);
    fileAppender.activateOptions();
    Logger.getRootLogger().addAppender(fileAppender);

    // Configure the office manager in a way that maximizes possible race conditions.
    final OfficeManager officeManager =
        LocalOfficeManager.builder()
            .portNumbers(2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009)
            // .portNumbers(2002, 2003)
            .maxTasksPerProcess(MAX_PROCESS_TASKS)
            .build();
    final LocalConverter converter = LocalConverter.make(officeManager);

    officeManager.start();
    try {
      final File source =
          new File(
              "src/integTest/resources/documents/test."
                  + Objects.requireNonNull(INPUT_FORMAT).getExtension());

      final Thread[] threads = new Thread[MAX_THREADS];

      boolean first = true;
      int threadCount = 0;

      for (int i = 0; i < MAX_CONVERSIONS; i++) {
        final File target =
            new File(
                testFolder,
                "test_" + i + "." + Objects.requireNonNull(OUTPUT_FORMAT).getExtension());
        target.deleteOnExit();

        // Converts the first document without threads to ensure everything is OK.
        if (first) {
          converter.convert(source).to(target).execute();
          first = false;
        }

        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("Creating thread " + threadCount);
        }
        final ConvertRunner runnable = new ConvertRunner(source, target, converter);
        threads[threadCount] = new Thread(runnable);
        threads[threadCount++].start();

        if (threadCount == MAX_THREADS) {
          for (int j = 0; j < threadCount; j++) {
            threads[j].join();
          }
          threadCount = 0;
        }
      }

      // Wait for remaining threads.
      for (int j = 0; j < threadCount; j++) {
        threads[j].join();
      }

    } finally {
      officeManager.stop();
      Logger.getRootLogger().removeAppender(console);
      Logger.getRootLogger().removeAppender(fileAppender);
    }
  }
}
