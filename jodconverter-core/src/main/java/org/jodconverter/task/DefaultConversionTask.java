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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.XComponent;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.uno.UnoRuntime;

import org.jodconverter.filter.FilterChain;
import org.jodconverter.job.SourceDocumentSpecs;
import org.jodconverter.job.TargetDocumentSpecs;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;

/** Represents the default behavior for a conversion task. */
@SuppressWarnings({
  "PMD.AvoidCatchingGenericException",
  "PMD.LawOfDemeter",
  "PMD.UseConcurrentHashMap"
})
public class DefaultConversionTask extends AbstractOfficeTask {

  private static final String ERROR_MESSAGE_STORE = "Could not store document: ";

  private static final Logger logger = LoggerFactory.getLogger(DefaultConversionTask.class);

  private final TargetDocumentSpecs target;
  private final FilterChain filterChain;

  /**
   * Creates a new conversion task from a specified source to a specified target.
   *
   * @param source The source specifications for the conversion.
   * @param target The target specifications for the conversion.
   * @param defaultLoadProperties The default properties to be applied when loading the document.
   *     These properties are added before the load properties of the document format specified in
   *     the {@code source} arguments.
   * @param filterChain The filter chain to use with this task.
   */
  public DefaultConversionTask(
      final SourceDocumentSpecs source,
      final TargetDocumentSpecs target,
      final Map<String, Object> defaultLoadProperties,
      final FilterChain filterChain) {
    super(source, defaultLoadProperties);

    this.target = target;
    this.filterChain = filterChain;
  }

  @Override
  public void execute(final OfficeContext context) throws OfficeException {

    logger.info("Executing default conversion task...");

    File sourceFile = null;
    File targetFile = null;
    try {
      // Obtain a source file that can be loaded by office. If the source if
      // an input stream, then a temporary file will be created from the
      // stream. The temporary file will be deleted once the task is done.
      sourceFile = source.getFile();

      XComponent document = null;
      try {
        document = loadDocument(context, sourceFile);
        modifyDocument(context, document);

        targetFile = target.getFile();
        storeDocument(document, targetFile);
        target.onComplete(targetFile);

      } catch (OfficeException officeEx) {
        target.onFailure(targetFile, officeEx);
        throw officeEx;
      } catch (Exception ex) {
        final OfficeException officeEx = new OfficeException("Conversion failed", ex);
        target.onFailure(targetFile, officeEx);
        throw officeEx;
      } finally {
        closeDocument(document);
      }

    } finally {

      // Here the source file is no longer required so we can delete
      // any temporary file that has been created if required.
      source.onConsumed(sourceFile);
    }
  }

  // Gets the office properties to apply when the converted
  // document will be saved as the output file.
  private Map<String, Object> getStoreProperties(final XComponent document) throws OfficeException {

    final Map<String, Object> storeProperties =
        new HashMap<>(
            target.getFormat().getStoreProperties(OfficeTaskUtils.getDocumentFamily(document)));
    if (target.getCustomStoreProperties() != null) {
      storeProperties.putAll(target.getCustomStoreProperties());
    }
    return storeProperties;
  }

  // Modifies the document after it has been loaded and before
  // it gets saved in the new format.
  void modifyDocument(final OfficeContext context, final XComponent document)
      throws OfficeException {

    if (filterChain != null) {
      filterChain.doFilter(context, document);
    }
  }

  // Stores the converted document as the output file.
  void storeDocument(final XComponent document, final File targetFile) throws OfficeException {

    final Map<String, Object> storeProperties = getStoreProperties(document);

    // The properties cannot be null
    Validate.notNull(storeProperties, "Unsupported conversion");

    try {
      UnoRuntime.queryInterface(XStorable.class, document)
          .storeToURL(toUrl(targetFile), toUnoProperties(storeProperties));
    } catch (ErrorCodeIOException errorCodeIoEx) {
      throw new OfficeException(
          ERROR_MESSAGE_STORE + targetFile.getName() + "; errorCode: " + errorCodeIoEx.ErrCode,
          errorCodeIoEx);
    } catch (IOException ioEx) {
      throw new OfficeException(ERROR_MESSAGE_STORE + targetFile.getName(), ioEx);
    }
  }
}
