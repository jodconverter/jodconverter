//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
//    -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
//    -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter.office;

import static org.artofsolving.jodconverter.office.OfficeUtils.toUrl;
import static org.testng.Assert.assertEquals;

import java.io.File;

public class OfficeUtilsTest {

    public void testToUrl() {
        //TODO create separate tests for Windows
        assertEquals(toUrl(new File("/tmp/document.odt")), "file:///tmp/document.odt");
        assertEquals(toUrl(new File("/tmp/document with spaces.odt")), "file:///tmp/document%20with%20spaces.odt");
    }

}
