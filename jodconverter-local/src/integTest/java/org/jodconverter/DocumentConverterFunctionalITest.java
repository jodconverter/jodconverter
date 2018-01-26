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

package org.jodconverter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DocumentConverterFunctionalITest extends AbstractOfficeITest {

  private static final int MAX_THREADS = 10;

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  /**
   * Test the conversion of an HTML file that contains an image.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void htmlWithImageConversion() throws Exception {

    final File source = new File(DOCUMENTS_DIR, "index.html");
    final File target = new File(testFolder.getRoot(), "index.pdf");

    // Convert the file to PDF
    LocalConverter.make().convert(source).to(target);
  }

  /**
   * Test the conversion of an HTML file that contains an image.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void testHtmlConversion() throws Exception {

    final File source = new File(DOCUMENTS_DIR, "test.html");
    final File target = new File(testFolder.getRoot(), "test.pdf");

    // Convert the file to PDF
    LocalConverter.make().convert(source).to(target);
  }

  /**
   * Test the conversion of all the supported documents format.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void runAllPossibleConversions() throws Exception {

    final Thread[] threads = new Thread[MAX_THREADS];
    int threadCount = 0;

    final AtomicReference<Exception> exception = new AtomicReference<Exception>();
    for (final File sourceFile :
        new File("src/integTest/resources/documents")
            .listFiles(
                new FilenameFilter() {
                  public boolean accept(final File dir, final String name) {
                    return name.startsWith("test.");
                  }
                })) {

      // Convert the file to all supported formats in a separated thread
      final Runnable runnable =
          new Runnable() {
            @Override
            public void run() {
              try {
                convertFileToAllSupportedFormats(sourceFile, testFolder.getRoot());
              } catch (Exception ex) {
                exception.set(ex);
              }
            }
          };

      //final Runner r = new Runner (source, target, RefreshFilter.CHAIN, converter);
      threads[threadCount] = new Thread(runnable);
      threads[threadCount++].start();

      if (threadCount == MAX_THREADS) {
        for (int j = 0; j < threadCount; j++) {
          threads[j].join();
        }
        threadCount = 0;
        if (exception.get() != null) {
          throw exception.get();
        }
      }

      //convertFileToAllSupportedFormats(sourceFile, testFolder.getRoot());
    }

    // Wait for remaining threads.
    for (int j = 0; j < threadCount; j++) {
      threads[j].join();
    }
    if (exception.get() != null) {
      throw exception.get();
    }
  }
}
