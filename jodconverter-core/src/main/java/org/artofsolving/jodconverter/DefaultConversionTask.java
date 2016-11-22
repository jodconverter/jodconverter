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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.filter.FilterChain;
import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;

import com.sun.star.lang.XComponent;

/**
 * Represents the default behavior for a conversion task.
 */
public class DefaultConversionTask extends AbstractConversionTask {

    private DocumentFormat inputFormat;
    private final DocumentFormat outputFormat;
    private Map<String, ?> defaultLoadProperties;
    private FilterChain filterChain;

    public DefaultConversionTask(File inputFile, File outputFile, DocumentFormat inputFormat, DocumentFormat outputFormat) {
        super(inputFile, outputFile);

        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
    }

    @Override
    protected Map<String, ?> getLoadProperties() throws OfficeException {

        Map<String, Object> loadProperties = new HashMap<String, Object>();
        if (defaultLoadProperties != null) {
            loadProperties.putAll(defaultLoadProperties);
        }
        if (inputFormat != null && inputFormat.getLoadProperties() != null) {
            loadProperties.putAll(inputFormat.getLoadProperties());
        }
        return loadProperties;
    }

    @Override
    protected Map<String, ?> getStoreProperties(XComponent document) throws OfficeException {

        DocumentFamily family = OfficeDocumentUtils.getDocumentFamily(document);
        return outputFormat.getStoreProperties(family);
    }

    // Don't allow override
    @Override
    protected final void modifyDocument(OfficeContext context, XComponent document) throws OfficeException {

        if (filterChain != null) {
            filterChain.doFilter(context, document);
        }
    }

    /**
     * Sets the default properties to be applied when the input document is loaded.
     * 
     * @param defaultLoadProperties
     *            the default properties to ne applied when loading the document.
     */
    public void setDefaultLoadProperties(Map<String, ?> defaultLoadProperties) {

        this.defaultLoadProperties = defaultLoadProperties;
    }

    /**
     * Sets the filter chain to be applied when modifying the document.
     * 
     * @param filterChain
     *            filterChain to use with this task.
     */
    public void setFilterChain(FilterChain filterChain) {

        this.filterChain = filterChain;
    }
}
