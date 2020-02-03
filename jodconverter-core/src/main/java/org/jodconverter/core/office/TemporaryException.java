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

package org.jodconverter.core.office;

/**
 * Represents an error condition that can be temporary, i.e. that could go away by simply retrying
 * the same operation after an interval.
 */
public class TemporaryException extends Exception {
  private static final long serialVersionUID = 7237380113208327295L;

  /**
   * Constructs a new exception with the specified message.
   *
   * @param message The message.
   */
  public TemporaryException(final String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified cause.
   *
   * @param cause The cause.
   */
  public TemporaryException(final Throwable cause) {
    super(cause);
  }
}
