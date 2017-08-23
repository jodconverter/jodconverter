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
import java.io.OutputStreamWriter;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import org.jodconverter.document.DocumentFormat;
import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.office.DefaultOfficeManager;
import org.jodconverter.office.OfficeManager;

public class StressITest {

  private static final Logger logger = Logger.getLogger(StressITest.class);

  private static final int MAX_CONVERSIONS = 1024;
  private static final int MAX_RUNNING_THREADS = 128;
  private static final int MAX_TASKS_PER_PROCESS = 10;

  private static final String INPUT_EXTENSION = "rtf";
  private static final String OUTPUT_EXTENSION = "pdf";

  /**
   * This test will run multiple parallel conversions, using 8 office processes. Just change the
   * MAX_* constants to control the numbers of conversion, threads and maximum conversion per office
   * process allowed.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void runParallelConversions() throws Exception {

    final String pattern = "%d{ISO8601} %-5p [%c{3}] [%t] %m%n";
    Logger.getRootLogger().removeAllAppenders();

    // Create console appender
    final ConsoleAppender console = new ConsoleAppender();
    console.setWriter(new OutputStreamWriter(System.out));
    console.setLayout(new PatternLayout(pattern));
    console.setThreshold(Level.DEBUG);
    console.activateOptions();
    Logger.getRootLogger().addAppender(console);

    // Keep a log file to be able to see if an error occurred
    final FileAppender fileAppender = new FileAppender();
    fileAppender.setName("FileLogger");
    fileAppender.setFile("test-output/" + StressITest.class.getSimpleName() + "/test.log");
    fileAppender.setLayout(new PatternLayout(pattern));
    fileAppender.setThreshold(Level.DEBUG);
    fileAppender.setAppend(true);
    fileAppender.activateOptions();
    Logger.getRootLogger().addAppender(fileAppender);

    // Configure the office manager in a way that maximizes possible race conditions.
    final OfficeManager officeManager =
        DefaultOfficeManager.builder()
            .portNumbers(2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009)
            .maxTasksPerProcess(MAX_TASKS_PER_PROCESS)
            .build();
    final DocumentConverter converter = DefaultConverter.make(officeManager);
    final DocumentFormatRegistry formatRegistry = converter.getFormatRegistry();

    officeManager.start();
    try {
      final File inputFile = new File("src/integTest/resources/documents/test." + INPUT_EXTENSION);

      final Thread[] threads = new Thread[MAX_RUNNING_THREADS];

      boolean first = true;
      int t = 0;

      final DocumentFormat inputFormat = formatRegistry.getFormatByExtension(INPUT_EXTENSION);
      final DocumentFormat outputFormat = formatRegistry.getFormatByExtension(OUTPUT_EXTENSION);
      for (int i = 0; i < MAX_CONVERSIONS; i++) {
        final File outputFile = File.createTempFile("test", "." + outputFormat.getExtension());
        outputFile.deleteOnExit();

        // Converts the first document without threads to ensure everything is OK.
        if (first) {
          converter.convert(inputFile).as(inputFormat).to(outputFile).as(outputFormat).execute();
          first = false;
        }

        logger.info("Creating thread " + t);
        final Runner r = new Runner(inputFile, outputFile, inputFormat, outputFormat, converter);
        threads[t] = new Thread(r);
        threads[t++].start();

        if (t == MAX_RUNNING_THREADS) {
          for (int j = 0; j < t; j++) {
            threads[j].join();
          }
          t = 0;
        }
      }

      // Wait for remaining threads.
      for (int j = 0; j < t; j++) {
        threads[j].join();
      }

    } finally {
      officeManager.stop();
    }
  }

  private class Runner implements Runnable {

    public Runner(
        final File inputFile,
        final File outputFile,
        final DocumentFormat inputFormat,
        final DocumentFormat outputFormat,
        final DocumentConverter converter) {
      super();

      this.inputFile = inputFile;
      this.outputFile = outputFile;
      this.inputFormat = inputFormat;
      this.outputFormat = outputFormat;
      this.converter = converter;
    }

    private final File inputFile;
    private final File outputFile;
    private final DocumentFormat inputFormat;
    private final DocumentFormat outputFormat;
    private final DocumentConverter converter;

    @Override
    public void run() {
      try {
        logger.info(
            "-- converting "
                + inputFormat.getExtension()
                + " to "
                + outputFormat.getExtension()
                + "... ");
        converter.convert(inputFile).as(inputFormat).to(outputFile).as(outputFormat).execute();
        logger.info("done.\n");
      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
      }
    }
  }
}
