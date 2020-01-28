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

package org.jodconverter.local.office;

import java.util.EventObject;

/** Event raised when an office connection gets opened or closed. */
class OfficeConnectionEvent extends EventObject {
  private static final long serialVersionUID = 2060652797570876077L;

  /**
   * Constructs a new event for the specified connection.
   *
   * @param source The connection on which the event initially occurred.
   */
  public OfficeConnectionEvent(final OfficeConnection source) {
    super(source);
  }
}
