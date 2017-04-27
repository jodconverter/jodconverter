/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.jodconverter.office;

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
