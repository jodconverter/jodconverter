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

package org.jodconverter.core.job;

import java.util.concurrent.CompletableFuture;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.office.OfficeException;

/** A fully specified conversion that is not yet applied to the converter. */
public interface ConversionJob {

  /**
   * Executes a conversion and blocks until the conversion terminates.
   *
   * @throws OfficeException If the conversion failed.
   */
  void execute() throws OfficeException;

  /**
   * Submits a conversion to the office manager and returns at once. The returned future completes
   * when the conversion is done, and completes exceptionally with an {@link OfficeException} when
   * the conversion fails. Cancelling the future abandons the conversion.
   *
   * <p>The actions chained to the future may run on a thread of the office manager: they must not
   * block.
   *
   * @return The future of the conversion.
   * @throws IllegalStateException If the office manager is not running.
   */
  @NonNull CompletableFuture<Void> executeAsync();

  /**
   * Specifies options that apply to the target document of this conversion only, such as {@link
   * org.jodconverter.core.pdf.PdfOptions} for a PDF document. The options must support the target
   * format, or the conversion will fail.
   *
   * @param options The options to apply to the target document.
   * @return The current conversion specification.
   * @throws UnsupportedOperationException If this conversion job does not support target options.
   */
  default @NonNull ConversionJob with(final @NonNull TargetOptions options) {
    throw new UnsupportedOperationException(
        getClass().getName() + " does not support target options");
  }

  /**
   * Gives the password that opens the source document of this conversion, when it is protected.
   * Without it, the conversion of a protected document fails with a {@code
   * PasswordProtectedException}; with a wrong one, it fails the same way.
   *
   * @param password The password of the source document.
   * @return The current conversion specification.
   * @throws UnsupportedOperationException If this conversion job cannot open protected documents.
   */
  default @NonNull ConversionJob password(final @NonNull String password) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support a password");
  }
}
