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

package org.jodconverter.local.office;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.office.AbstractRetryable;
import org.jodconverter.core.office.TemporaryException;

/** Performs a connection to an office process. */
public class ConnectRetryable extends AbstractRetryable<OfficeConnectionException> {

  private final OfficeConnection connection;

  /**
   * Creates a new instance of the class for the specified process and connection.
   *
   * @param connection The office connection to connect.
   */
  public ConnectRetryable(final @NonNull OfficeConnection connection) {
    super();

    this.connection = connection;
  }

  @Override
  protected void attempt() throws TemporaryException {

    try {
      // Try to connect
      connection.connect();

    } catch (OfficeConnectionException ex) {

      // Throw a TemporaryException
      throw new TemporaryException(ex);
    }
  }
}
