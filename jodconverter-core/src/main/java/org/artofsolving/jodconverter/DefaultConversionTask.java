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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.step.RefreshStep;
import org.artofsolving.jodconverter.step.TransformerStep;

import com.sun.star.lang.XComponent;

/**
 * Represents the default behavior for a conversion task.
 */
public class DefaultConversionTask extends AbstractConversionTask {

    private List<TransformerStep> transformerSteps;
    private Map<String, ?> defaultLoadProperties;
    private DocumentFormat inputFormat;
    private final DocumentFormat outputFormat;

    public DefaultConversionTask(File inputFile, File outputFile, DocumentFormat inputFormat, DocumentFormat outputFormat) {
        super(inputFile, outputFile);

        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
        this.transformerSteps = new ArrayList<>();

    }

    /**
     * Adda the default transformer steps to this conversion task.
     */
    protected void addDefaultTransformerSteps() {

        addTransformerStep(new RefreshStep());
    }

    /**
     * Adds a tranformer step to be applied after the document is loaded and before it is stored in
     * the new document format. A transformer step is used to modify the document before the
     * conversion. Transformer steps are applied in the same order there are added to this
     * conversion task.
     */
    public void addTransformerStep(TransformerStep step) {

        transformerSteps.add(step);
    }

    @Override
    protected Map<String, ?> getLoadProperties() {

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
    protected Map<String, ?> getStoreProperties(XComponent document) {

        DocumentFamily family = OfficeDocumentUtils.getDocumentFamily(document);
        return outputFormat.getStoreProperties(family);
    }

    @Override
    protected void modifyDocument(OfficeContext context, XComponent document) throws OfficeException {

        for (TransformerStep step : transformerSteps) {
            step.transform(context, document);
        }
    }

    /**
     * Sets the default properties to be applied when the input document is loaded.
     */
    public void setDefaultLoadProperties(Map<String, ?> defaultLoadProperties) {

        this.defaultLoadProperties = defaultLoadProperties;
    }

}
