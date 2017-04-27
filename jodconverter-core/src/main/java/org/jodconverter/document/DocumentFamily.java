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

package org.jodconverter.document;

/** Represents a document type supported by office. */
public enum DocumentFamily {

  /** Text documents (odt, doc, docx, rtf, etc.) */
  TEXT,

  /** Spreadsheet documents (ods, xls, xlsx, csv, etc.) */
  SPREADSHEET,

  /** Spreadsheet documents (odp, ppt, pptx, etc.) */
  PRESENTATION,

  /** Drawing documents (odg, png, svg, etc.) */
  DRAWING
}
