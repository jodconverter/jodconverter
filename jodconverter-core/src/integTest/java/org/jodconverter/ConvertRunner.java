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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import org.jodconverter.document.DocumentFormat;
import org.jodconverter.filter.FilterChain;

/**
 * Runnable used to convert a document. This kind of runner is useful when a conversion must be done
 * in his own thread.
 */
public class ConvertRunner implements Runnable {

  private static final Logger logger = Logger.getLogger(ConvertRunner.class);

  /**
   * Constructs a new runner with the specified arguments.
   *
   * @param source The source file.
   * @param target The target file.
   * @param filterChain The chain that will modified the document.
   * @param converter The converter that will do the conversion.
   */
  public ConvertRunner(
      final File source,
      final File target,
      final FilterChain filterChain,
      final DocumentConverter converter) {
    super();

    this.source = source;
    this.target = target;
    this.filterChain = filterChain;
    this.converter = converter;
  }

  private final File source;
  private final File target;
  private final FilterChain filterChain;
  private final DocumentConverter converter;

  @Override
  public void run() {

    final DocumentFormat sourceFormat =
        converter
            .getFormatRegistry()
            .getFormatByExtension(FilenameUtils.getExtension(source.getName()));

    final DocumentFormat targetFormat =
        converter
            .getFormatRegistry()
            .getFormatByExtension(FilenameUtils.getExtension(target.getName()));

    try {

      logger.info(
          "-- converting "
              + sourceFormat.getExtension()
              + " to "
              + targetFormat.getExtension()
              + "... ");
      converter.convert(source).filterWith(filterChain).to(target).execute();
      logger.info("done.\n");

      // Check that the created file is not empty. The programmer still have to
      // manually if the content of the output file looks good.
      assertThat(source).isFile();
      assertThat(source.length()).isGreaterThan(0L);

    } catch (Exception ex) {

      // Log the error.
      final String message =
          "Unable to convert from "
              + sourceFormat.getExtension()
              + " to "
              + targetFormat.getExtension()
              + ".";
      if (ex.getCause() instanceof com.sun.star.task.ErrorCodeIOException) {
        final com.sun.star.task.ErrorCodeIOException ioEx =
            (com.sun.star.task.ErrorCodeIOException) ex.getCause();
        logger.error(message + " " + ioEx.getMessage(), ioEx);
      } else {
        logger.error(message + " " + ex.getMessage(), ex);
      }

      throw new RuntimeException(ex); // NOSONAR
    }
  }
}
