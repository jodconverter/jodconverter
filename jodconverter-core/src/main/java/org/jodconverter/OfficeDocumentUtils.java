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

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.UnoRuntime;

import org.jodconverter.document.DocumentFamily;
import org.jodconverter.office.OfficeException;

final class OfficeDocumentUtils {

  private OfficeDocumentUtils() {
    throw new AssertionError("Utility class must not be instantiated");
  }

  public static DocumentFamily getDocumentFamily(final XComponent document) throws OfficeException {

    final XServiceInfo serviceInfo = UnoRuntime.queryInterface(XServiceInfo.class, document);
    if (serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")) {
      // NOTE: a GenericTextDocument is either a TextDocument, a WebDocument, or a GlobalDocument
      // but this further distinction doesn't seem to matter for conversions
      return DocumentFamily.TEXT;
    } else if (serviceInfo.supportsService("com.sun.star.sheet.SpreadsheetDocument")) {
      return DocumentFamily.SPREADSHEET;
    } else if (serviceInfo.supportsService("com.sun.star.presentation.PresentationDocument")) {
      return DocumentFamily.PRESENTATION;
    } else if (serviceInfo.supportsService("com.sun.star.drawing.DrawingDocument")) {
      return DocumentFamily.DRAWING;
    } else {
      throw new OfficeException(
          "Document of unknown family: " + serviceInfo.getImplementationName());
    }
  }
}
