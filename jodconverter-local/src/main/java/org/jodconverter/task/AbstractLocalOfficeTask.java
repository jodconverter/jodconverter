/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

package org.jodconverter.task;

import static org.jodconverter.office.LocalOfficeUtils.toUnoProperties;
import static org.jodconverter.office.LocalOfficeUtils.toUrl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;

import org.jodconverter.LocalConverter;
import org.jodconverter.job.SourceDocumentSpecs;
import org.jodconverter.office.LocalOfficeContext;
import org.jodconverter.office.OfficeException;

/**
 * Base class for all local office tasks implementation.
 *
 * @see OfficeTask
 */
public abstract class AbstractLocalOfficeTask extends AbstractOfficeTask {

  private static final String ERROR_MESSAGE_LOAD = "Could not open document: ";
  protected final Map<String, Object> loadProperties;

  protected static void appendProperties(
      final Map<String, Object> properties, final Map<String, Object> toAddProperties) {

    Optional.ofNullable(toAddProperties).ifPresent(properties::putAll);
  }

  /**
   * Creates a new task with the specified source document.
   *
   * @param source The source specifications of the document.
   */
  public AbstractLocalOfficeTask(final SourceDocumentSpecs source) {
    this(source, null);
  }

  /**
   * Creates a new task with the specified source document.
   *
   * @param source The source specifications of the document.
   * @param loadProperties The load properties to be applied when loading the document. These
   *     properties are added before the load properties of the document format specified in the
   *     {@code source} arguments.
   */
  public AbstractLocalOfficeTask(
      final SourceDocumentSpecs source, final Map<String, Object> loadProperties) {
    super(source);

    this.loadProperties = loadProperties;
  }

  // Gets the office properties to apply when the input file will be loaded.
  protected Map<String, Object> getLoadProperties() {

    final Map<String, Object> loadProps =
        new HashMap<>(
            Optional.ofNullable(loadProperties).orElse(LocalConverter.DEFAULT_LOAD_PROPERTIES));
    Optional.ofNullable(source.getFormat())
        .ifPresent(fmt -> appendProperties(loadProps, fmt.getLoadProperties()));

    return loadProps;
  }

  // Loads the document from the specified source file.
  protected XComponent loadDocument(final LocalOfficeContext context, final File sourceFile)
      throws OfficeException {

    try {
      final XComponent document =
          context
              .getComponentLoader()
              .loadComponentFromURL(
                  toUrl(sourceFile), "_blank", 0, toUnoProperties(getLoadProperties()));

      // The document cannot be null
      Validate.notNull(document, ERROR_MESSAGE_LOAD + sourceFile.getName());
      return document;

    } catch (IllegalArgumentException illegalArgumentEx) {
      throw new OfficeException(ERROR_MESSAGE_LOAD + sourceFile.getName(), illegalArgumentEx);
    } catch (ErrorCodeIOException errorCodeIoEx) {
      throw new OfficeException(
          ERROR_MESSAGE_LOAD + sourceFile.getName() + "; errorCode: " + errorCodeIoEx.ErrCode,
          errorCodeIoEx);
    } catch (IOException ioEx) {
      throw new OfficeException(ERROR_MESSAGE_LOAD + sourceFile.getName(), ioEx);
    }
  }

  // Closes the specified document.
  protected void closeDocument(final XComponent document) {

    if (document != null) {

      // Closing the converted document. Use XCloseable.close if the
      // interface is supported, otherwise use XComponent.dispose
      final XCloseable closeable = UnoRuntime.queryInterface(XCloseable.class, document);
      if (closeable == null) {
        // If close is not supported by this model - try to dispose it.
        UnoRuntime.queryInterface(XComponent.class, document).dispose();
      } else {
        try {
          // The boolean parameter deliverOwnership tells objects vetoing the
          // close process that they may assume ownership if they object the closure
          // by throwing a CloseVetoException. Here we give up ownership. To be on
          // the safe side, catch possible veto exception anyway.
          closeable.close(true);
        } catch (CloseVetoException closeVetoEx) { // NOSONAR
          // whoever raised the veto should close the document
        }
      }
    }
  }
}
