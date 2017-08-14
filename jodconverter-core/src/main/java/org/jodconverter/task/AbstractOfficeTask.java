/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

import static org.jodconverter.office.OfficeUtils.toUnoProperties;
import static org.jodconverter.office.OfficeUtils.toUrl;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import com.sun.star.document.UpdateDocMode;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;

import org.jodconverter.job.SourceDocumentSpecs;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeTask;

/**
 * Base class for all office tasks implementation.
 *
 * @see OfficeTask
 */
public abstract class AbstractOfficeTask implements OfficeTask {

  private static final String ERROR_MESSAGE_LOAD = "Could not open document: ";
  protected static final Map<String, Object> DEFAULT_LOAD_PROPERTIES;

  static {
    final Map<String, Object> loadProperties = new HashMap<>();
    loadProperties.put("Hidden", true);
    loadProperties.put("ReadOnly", true);
    loadProperties.put("UpdateDocMode", UpdateDocMode.QUIET_UPDATE);
    DEFAULT_LOAD_PROPERTIES = Collections.unmodifiableMap(loadProperties);
  }

  // Provides default properties to use when we load (open) a document before
  // a conversion, regardless the input type of the document.
  private static Map<String, Object> createDefaultLoadProperties() {

    return new HashMap<>(DEFAULT_LOAD_PROPERTIES);
  }

  protected final SourceDocumentSpecs source;
  protected Map<String, Object> defaultLoadProperties;

  /**
   * Creates a new task with the specified source document.
   *
   * @param source The source specifications of the document.
   */
  public AbstractOfficeTask(final SourceDocumentSpecs source) {
    this(source, null);
  }

  /**
   * Creates a new task with the specified source document.
   *
   * @param source The source specifications of the document.
   * @param defaultLoadProperties The default properties to be applied when loading the document.
   *     These properties are added before the load properties of the document format specified in
   *     the {@code source} arguments.
   */
  public AbstractOfficeTask(
      final SourceDocumentSpecs source, final Map<String, Object> defaultLoadProperties) {
    super();

    this.source = source;
    this.defaultLoadProperties =
        defaultLoadProperties == null ? createDefaultLoadProperties() : defaultLoadProperties;
  }

  // Gets the office properties to apply when the input file will be loaded.
  protected Map<String, Object> getLoadProperties() {

    final Map<String, Object> loadProperties = new HashMap<>();
    if (defaultLoadProperties != null) {
      loadProperties.putAll(defaultLoadProperties);
    }
    if (source.getFormat() != null && source.getFormat().getLoadProperties() != null) {
      loadProperties.putAll(source.getFormat().getLoadProperties());
    }
    if (source.getCustomLoadProperties() != null) {
      loadProperties.putAll(source.getCustomLoadProperties());
    }
    return loadProperties;
  }

  // Loads the document from the specified source file.
  protected XComponent loadDocument(final OfficeContext context, final File sourceFile)
      throws OfficeException {

    XComponent document = null;
    try {
      document =
          context
              .getComponentLoader()
              .loadComponentFromURL(
                  toUrl(sourceFile), "_blank", 0, toUnoProperties(getLoadProperties()));
    } catch (IllegalArgumentException illegalArgumentEx) {
      throw new OfficeException(ERROR_MESSAGE_LOAD + sourceFile.getName(), illegalArgumentEx);
    } catch (ErrorCodeIOException errorCodeIoEx) {
      throw new OfficeException(
          ERROR_MESSAGE_LOAD + sourceFile.getName() + "; errorCode: " + errorCodeIoEx.ErrCode,
          errorCodeIoEx);
    } catch (IOException ioEx) {
      throw new OfficeException(ERROR_MESSAGE_LOAD + sourceFile.getName(), ioEx);
    }

    // The document cannot be null
    Validate.notNull(document, ERROR_MESSAGE_LOAD + sourceFile.getName());
    return document;
  }

  // Closes the specified document.
  protected void closeDocument(final XComponent document) {

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
