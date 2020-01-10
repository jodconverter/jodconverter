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

package org.jodconverter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.document.DocumentFormat;
import org.jodconverter.filter.FilterChain;

/**
 * Runnable used to convert a document. This kind of runner is useful when a conversion must be done
 * in his own thread.
 */
public class ConvertRunner implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConvertRunner.class);

  private final File source;
  private final File target;
  private final FilterChain filterChain;

  /**
   * Constructs a new runner with the specified arguments.
   *
   * @param source The source file.
   * @param target The target file.
   * @param filterChain The chain that will modified the document.
   */
  public ConvertRunner(final File source, final File target, final FilterChain filterChain) {
    super();

    this.source = source;
    this.target = target;
    this.filterChain = filterChain;
  }

  @Override
  public void run() {

    final LocalConverter converter = LocalConverter.make();

    final DocumentFormat sourceFmt =
        converter
            .getFormatRegistry()
            .getFormatByExtension(FilenameUtils.getExtension(source.getName()));
    final String sourceExt = sourceFmt.getExtension();

    final DocumentFormat targetFmt =
        converter
            .getFormatRegistry()
            .getFormatByExtension(FilenameUtils.getExtension(target.getName()));
    final String targetExt = targetFmt.getExtension();

    try {

      LOGGER.info("Converting [{} -> {}]...", sourceExt, targetExt);
      converter.setFilterChain(filterChain).convert(source).to(target).execute();
      LOGGER.info("Conversion done [{} -> {}]...", sourceExt, targetExt);

      // Check that the created file is not empty. The programmer still have to
      // manually if the content of the output file looks good.
      assertThat(source).isFile();
      assertThat(source.length()).isGreaterThan(0L);

    } catch (Exception ex) {

      // Log the error.
      final String message = "Unable to convert from " + sourceExt + " to " + targetExt + ".";
      if (ex.getCause() instanceof com.sun.star.task.ErrorCodeIOException) {
        final com.sun.star.task.ErrorCodeIOException ioEx =
            (com.sun.star.task.ErrorCodeIOException) ex.getCause();
        LOGGER.error(message + " " + ioEx.getMessage(), ioEx);
      } else {
        LOGGER.error(message + " " + ex.getMessage(), ex);
      }

      throw new RuntimeException(ex);
    }
  }
}
