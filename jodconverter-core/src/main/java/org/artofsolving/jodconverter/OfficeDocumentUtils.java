//
// JODConverter - Java OpenDocument Converter
// Copyright 2009 Art of Solving Ltd
// Copyright 2004-2009 Mirko Nasato
//
// JODConverter is free software: you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
//
// JODConverter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General
// Public License along with JODConverter.  If not, see
// <http://www.gnu.org/licenses/>.
//
package org.artofsolving.jodconverter;

import static org.artofsolving.jodconverter.office.OfficeUtils.*;

import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.office.OfficeException;


import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;

class OfficeDocumentUtils {

    private OfficeDocumentUtils() {
        throw new AssertionError("utility class must not be instantiated");
    }

    public static DocumentFamily getDocumentFamily(XComponent document) throws OfficeException {
        XServiceInfo serviceInfo = cast(XServiceInfo.class, document);
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
            throw new OfficeException("document of unknown family: " + serviceInfo.getImplementationName());
        }
    }

}
