//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
// -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
// -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter;

import static org.artofsolving.jodconverter.office.OfficeUtils.toUnoProperties;
import static org.artofsolving.jodconverter.office.OfficeUtils.toUrl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;

import com.sun.star.document.UpdateDocMode;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.uno.UnoRuntime;

public class OfficeDocumentUtils {
	
	private static Map<String, Object> defaultLoadProperties=null;

    private OfficeDocumentUtils() {
        throw new AssertionError("Utility class must not be instantiated");
    }

    public static DocumentFamily getDocumentFamily(XComponent document) throws OfficeException {

        XServiceInfo serviceInfo = UnoRuntime.queryInterface(XServiceInfo.class, document);
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
            throw new OfficeException("Document of unknown family: " + serviceInfo.getImplementationName());
        }
    }
    
    public static XComponent loadDocument(String source, OfficeContext context) throws OfficeException {
    	XComponent document=null;
    	File inputFile=new File(source);
    	
		defaultLoadProperties=createDefaultLoadProperties();

        // Check if the file exists
        if (!inputFile.exists()) {
            throw new OfficeException("Input document not found");
        }

        try {
            document = context.getComponentLoader().loadComponentFromURL(toUrl(inputFile), "_blank", 0, toUnoProperties(getLoadProperties()));
        } catch (IllegalArgumentException illegalArgumentEx) {
            throw new OfficeException("Could not load document: " + inputFile.getName(), illegalArgumentEx);
        } catch (ErrorCodeIOException errorCodeIOEx) {
            throw new OfficeException("Could not load document: " + inputFile.getName() + "; errorCode: " + errorCodeIOEx.ErrCode, errorCodeIOEx);
        } catch (IOException ioEx) {
            throw new OfficeException("Could not load document: " + inputFile.getName(), ioEx);
        }
        if (document == null) {
            throw new OfficeException("Could not load document: " + inputFile.getName());
        }
    	
    	return document;
  
    }

	protected static Map<String, ?> getLoadProperties() throws OfficeException {
	
	    Map<String, Object> loadProperties = new HashMap<String, Object>();
	    if (defaultLoadProperties != null) {
	        loadProperties.putAll(defaultLoadProperties);
	    }
	    
	    return loadProperties;
	}
	
	/**
	 * Sets the default properties to be applied when the input document is loaded.
	 * 
	 * @param defaultLoadProperties
	 *            the default properties to ne applied when loading the document.
	 */
	protected void setDefaultLoadProperties(HashMap<String, Object> defaultLoadProperties) {
	
	    OfficeDocumentUtils.defaultLoadProperties = defaultLoadProperties;
	}
	
	
	// Provides default properties to use when we load (open) a document before
	// a conversion, regardless the input type of the document.
	protected static Map<String, Object> createDefaultLoadProperties() {
	
	    Map<String, Object> loadProperties = new HashMap<String, Object>();
	    loadProperties.put("Hidden", true);
	    loadProperties.put("ReadOnly", false);
	    loadProperties.put("UpdateDocMode", UpdateDocMode.QUIET_UPDATE);
	    return loadProperties;
	}

}
