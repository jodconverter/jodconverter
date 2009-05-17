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
package org.artofsolving.jodconverter.document;

import static org.testng.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.document.JsonDocumentFormatRegistry;

import org.json.JSONException;
import org.testng.annotations.Test;

@Test
public class JsonDocumentFormatRegistryTest {

    public void readJsonRegistry() throws JSONException, IOException {
        InputStream input = getClass().getResourceAsStream("/document-formats.js");
        DocumentFormatRegistry registry = null;
        try {
            registry = new JsonDocumentFormatRegistry(input);
        } finally {
            IOUtils.closeQuietly(input);
        }
        DocumentFormat odt = registry.getFormatByExtension("odt");
        assertNotNull(odt);
        assertNotNull(odt.getStoreProperties(DocumentFamily.TEXT));
    }

}
