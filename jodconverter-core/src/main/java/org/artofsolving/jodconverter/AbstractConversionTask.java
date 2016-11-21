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
import java.util.Map;

import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeTask;

import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;

/**
 * Base class for all tasks that can be executed by an office process.
 */
public abstract class AbstractConversionTask implements OfficeTask {

    protected final File inputFile;
    protected final File outputFile;

    /**
     * Initializes a new instance of the class with the specified input and output file.
     * 
     * @param inputFile
     *            the input file to convert.
     * @param outputFile
     *            the output file of the conversion.
     */
    public AbstractConversionTask(File inputFile, File outputFile) {

        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    @Override
    public void execute(OfficeContext context) throws OfficeException {

        XComponent document = null;
        try {
            document = loadDocument(context);
            modifyDocument(context, document);
            storeDocument(document);
        } catch (OfficeException officeEx) {
            throw officeEx;
        } catch (Exception ex) {
            throw new OfficeException("Conversion failed", ex);
        } finally {
            if (document != null) {

                // Closing the converted document. Use XCloseable.close if the
                // interface is supported, otherwise use XComponent.dispose
                XCloseable closeable = UnoRuntime.queryInterface(XCloseable.class, document);
                if (closeable != null) {
                    try {
                        closeable.close(true);
                    } catch (CloseVetoException closeVetoEx) {
                        // whoever raised the veto should close the document
                    }
                } else {
                    UnoRuntime.queryInterface(XComponent.class, document).dispose();
                }
            }
        }
    }

    /**
     * Gets the office properties to apply when the input file will be loaded.
     */
    protected abstract Map<String, ?> getLoadProperties() throws OfficeException;

    /**
     * Gets the office properties to apply when the converted document will be saved as the output
     * file.
     */
    protected abstract Map<String, ?> getStoreProperties(XComponent document) throws OfficeException;

    // Load the document to convert
    private XComponent loadDocument(OfficeContext context) throws OfficeException {

        // Check if the file exists
        if (!inputFile.exists()) {
            throw new OfficeException("Input document not found");
        }

        XComponent document = null;
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

    /**
     * Override to modify the document after it has been loaded and before it gets saved in the new
     * format.
     * <p>
     * Does nothing by default.
     * 
     * @param context
     *            the office context.
     * @param document
     *            the office document.
     * @throws OfficeException
     *             if an error occurs.
     */
    protected void modifyDocument(OfficeContext context, XComponent document) throws OfficeException {}

    // Stores the converted document as the ouput file.
    private void storeDocument(XComponent document) throws OfficeException {

        Map<String, ?> storeProperties = getStoreProperties(document);
        if (storeProperties == null) {
            throw new OfficeException("Unsupported conversion");
        }
        try {
            UnoRuntime.queryInterface(XStorable.class, document).storeToURL(toUrl(outputFile), toUnoProperties(storeProperties));
        } catch (ErrorCodeIOException errorCodeIOEx) {
            throw new OfficeException("could not store document: " + outputFile.getName() + "; errorCode: " + errorCodeIOEx.ErrCode, errorCodeIOEx);
        } catch (IOException ioEx) {
            throw new OfficeException("could not store document: " + outputFile.getName(), ioEx);
        }
    }

}
