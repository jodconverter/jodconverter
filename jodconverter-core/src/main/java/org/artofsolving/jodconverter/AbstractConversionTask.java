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

package org.artofsolving.jodconverter;

import static org.artofsolving.jodconverter.office.OfficeUtils.toUnoProperties;
import static org.artofsolving.jodconverter.office.OfficeUtils.toUrl;

import java.io.File;
import java.util.Map;

import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;

import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeTask;
import org.artofsolving.jodconverter.office.ValidateUtils;

/** Base class for all tasks that can be executed by an office process. */
public abstract class AbstractConversionTask implements OfficeTask {

  private static final String ERROR_MESSAGE_LOAD = "Could not open document: ";
  private static final String ERROR_MESSAGE_STORE = "Could not store document: ";

  private final File inputFile;
  private final File outputFile;

  /**
   * Initializes a new instance of the class with the specified input and output file.
   *
   * @param inputFile the input file to convert.
   * @param outputFile the output file of the conversion.
   */
  public AbstractConversionTask(final File inputFile, final File outputFile) {

    this.inputFile = inputFile;
    this.outputFile = outputFile;
  }

  @Override
  public void execute(final OfficeContext context) throws OfficeException {

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
        final XCloseable closeable = UnoRuntime.queryInterface(XCloseable.class, document);
        if (closeable == null) {
          UnoRuntime.queryInterface(XComponent.class, document).dispose();
        } else {
          try {
            closeable.close(true);
          } catch (CloseVetoException closeVetoEx) { // NOSONAR
            // whoever raised the veto should close the document
          }
        }
      }
    }
  }

  /** Gets the office properties to apply when the input file will be loaded. */
  protected abstract Map<String, ?> getLoadProperties() throws OfficeException;

  /**
   * Gets the office properties to apply when the converted document will be saved as the output
   * file.
   */
  protected abstract Map<String, ?> getStoreProperties(XComponent document) throws OfficeException;

  // Load the document to convert
  private XComponent loadDocument(final OfficeContext context) throws OfficeException {

    // Check if the file exists
    ValidateUtils.fileExists(inputFile, "Input document not found: %s");

    XComponent document = null;
    try {
      document =
          context
              .getComponentLoader()
              .loadComponentFromURL(
                  toUrl(inputFile), "_blank", 0, toUnoProperties(getLoadProperties()));
    } catch (IllegalArgumentException illegalArgumentEx) {
      throw new OfficeException(ERROR_MESSAGE_LOAD + inputFile.getName(), illegalArgumentEx);
    } catch (ErrorCodeIOException errorCodeIoEx) {
      throw new OfficeException(
          ERROR_MESSAGE_LOAD + inputFile.getName() + "; errorCode: " + errorCodeIoEx.ErrCode,
          errorCodeIoEx);
    } catch (IOException ioEx) {
      throw new OfficeException(ERROR_MESSAGE_LOAD + inputFile.getName(), ioEx);
    }

    // The document cannot be null
    ValidateUtils.notNull(document, ERROR_MESSAGE_LOAD + inputFile.getName());
    return document;
  }

  /**
   * Override to modify the document after it has been loaded and before it gets saved in the new
   * format.
   *
   * @param context the office context.
   * @param document the office document.
   * @throws OfficeException if an error occurs.
   */
  protected abstract void modifyDocument(final OfficeContext context, final XComponent document)
      throws OfficeException;

  // Stores the converted document as the ouput file.
  private void storeDocument(final XComponent document) throws OfficeException {

    final Map<String, ?> storeProperties = getStoreProperties(document);

    // The properties cannot be null
    ValidateUtils.notNull(storeProperties, "Unsupported conversion");

    try {
      UnoRuntime.queryInterface(XStorable.class, document)
          .storeToURL(toUrl(outputFile), toUnoProperties(storeProperties));
    } catch (ErrorCodeIOException errorCodeIoEx) {
      throw new OfficeException(
          ERROR_MESSAGE_STORE + outputFile.getName() + "; errorCode: " + errorCodeIoEx.ErrCode,
          errorCodeIoEx);
    } catch (IOException ioEx) {
      throw new OfficeException(ERROR_MESSAGE_STORE + outputFile.getName(), ioEx);
    }
  }
}
