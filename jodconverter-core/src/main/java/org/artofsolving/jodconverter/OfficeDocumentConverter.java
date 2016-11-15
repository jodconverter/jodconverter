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

import org.apache.commons.io.FilenameUtils;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;

import com.sun.star.document.UpdateDocMode;

/**
 * A OfficeDocumentConverter is responsible to execute the conversion of documents using an office
 * manager.
 */
public class OfficeDocumentConverter {

    private final OfficeManager officeManager;
    private final DocumentFormatRegistry formatRegistry;
    private Map<String, ?> defaultLoadProperties = createDefaultLoadProperties();

    /**
     * Constructs a new instance of the class with the specified manager.
     * 
     * @param officeManager
     *            the manager that will provide the office instance required for a conversion.
     */
    public OfficeDocumentConverter(OfficeManager officeManager) {

        this(officeManager, new DefaultDocumentFormatRegistry());
    }

    /**
     * Constructs a new instance of the class with the specified manager and registry.
     * 
     * @param officeManager
     *            the manager that will provide the office instance required for a conversion.
     * @param formatRegistry
     *            a collections of {@link DocumentFormat} supported by this converter.
     */
    public OfficeDocumentConverter(OfficeManager officeManager, DocumentFormatRegistry formatRegistry) {

        this.officeManager = officeManager;
        this.formatRegistry = formatRegistry;
    }

    /**
     * Converts an input file to an output file. The files extensions are used to determine the
     * input and output {@link DocumentFormat}.
     * 
     * @param inputFile
     *            the input file to convert.
     * @param outputFile
     *            the target output file.
     * @throws OfficeException
     *             if the conversion fails.
     */
    public void convert(File inputFile, File outputFile) throws OfficeException {

        String outputExtension = FilenameUtils.getExtension(outputFile.getName());
        DocumentFormat outputFormat = formatRegistry.getFormatByExtension(outputExtension);
        convert(inputFile, outputFile, outputFormat);
    }

    /**
     * Converts an input file to an output file. The input file extension is used to determine the
     * input {@link DocumentFormat}.
     * 
     * @param inputFile
     *            the input file to convert.
     * @param outputFile
     *            the target output file.
     * @param outputFormat
     *            the target output format.
     * @throws OfficeException
     *             if the conversion fails.
     */
    public void convert(File inputFile, File outputFile, DocumentFormat outputFormat) throws OfficeException {

        String inputExtension = FilenameUtils.getExtension(inputFile.getName());
        DocumentFormat inputFormat = formatRegistry.getFormatByExtension(inputExtension);
        DefaultConversionTask conversionTask = new DefaultConversionTask(inputFile, outputFile, inputFormat, outputFormat);
        conversionTask.setDefaultLoadProperties(defaultLoadProperties);
        officeManager.execute(conversionTask);
    }

    // Provides default properties to use when we load (open) a document before
    // a conversion, regardless the input type of the document.
    private Map<String, Object> createDefaultLoadProperties() {

        Map<String, Object> loadProperties = new HashMap<String, Object>();
        loadProperties.put("Hidden", true);
        loadProperties.put("ReadOnly", true);
        loadProperties.put("UpdateDocMode", UpdateDocMode.QUIET_UPDATE);
        return loadProperties;
    }

    /**
     * Gets the {@link DocumentFormat} supported by the converter.
     */
    public DocumentFormatRegistry getFormatRegistry() {

        return formatRegistry;
    }

    /**
     * Sets the default properties to use when we load (open) a document before a conversion,
     * regardless the input type of the document.
     */
    public void setDefaultLoadProperties(Map<String, ?> defaultLoadProperties) {

        this.defaultLoadProperties = defaultLoadProperties;
    }

}
