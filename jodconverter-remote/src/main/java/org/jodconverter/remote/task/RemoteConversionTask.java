/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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

package org.jodconverter.remote.task;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.job.SourceDocumentSpecs;
import org.jodconverter.core.job.TargetDocumentSpecs;
import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.remote.office.RemoteOfficeContext;
import org.jodconverter.remote.office.RequestConfig;

/** Represents the default behavior for an remote conversion task. */
public class RemoteConversionTask extends AbstractRemoteOfficeTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConversionTask.class);
  private static final String FILTER_DATA = "FilterData";
  private static final String FILTER_DATA_PREFIX_PARAM = "fd";
  private static final String LOAD_PROPERTIES_PREFIX_PARAM = "l";
  private static final String STORE_PROPERTIES_PREFIX_PARAM = "s";

  private final TargetDocumentSpecs target;

  /**
   * Creates a new conversion task from a specified source to a specified target.
   *
   * @param source The source specifications for the conversion.
   * @param target The target specifications for the conversion.
   */
  public RemoteConversionTask(final SourceDocumentSpecs source, final TargetDocumentSpecs target) {
    super(source);

    this.target = target;
  }

  @SuppressWarnings("unchecked")
  private void addPropertiesToBuilder(
      final URIBuilder uriBuilder,
      final Map<String, Object> properties,
      final String parameterPrefix) {

    if (properties != null && !properties.isEmpty()) {
      for (final Map.Entry<String, Object> entry : properties.entrySet()) {
        final String key = entry.getKey();
        final Object value = entry.getValue();

        // First, check if we are dealing with the FilterData property
        if (FILTER_DATA.equalsIgnoreCase(key) && value instanceof Map) {
          // Add all the FilterData properties
          for (final Map.Entry<String, Object> fdentry : ((Map<String, Object>) value).entrySet()) {
            uriBuilder.addParameter(
                parameterPrefix + FILTER_DATA_PREFIX_PARAM + fdentry.getKey(),
                fdentry.getValue().toString());
          }
        } else if (value instanceof String || value.getClass().isPrimitive()) {
          uriBuilder.addParameter(parameterPrefix + key, value.toString());
        }
      }
    }
  }

  @Override
  public void execute(final OfficeContext context) throws OfficeException {

    LOGGER.info("Executing remote conversion task...");
    final RemoteOfficeContext remoteContext = (RemoteOfficeContext) context;

    // Obtain a source file that can be loaded by office. If the source
    // is an input stream, then a temporary file will be created from the
    // stream. The temporary file will be deleted once the task is done.
    final File sourceFile = source.getFile();
    try {

      // Get the target file (which is a temporary file if the
      // output target is an output stream).
      final File targetFile = target.getFile();

      try {
        // TODO: Add the ability to pass on a custom charset to FileBody

        // See https://github.com/LibreOffice/online/blob/master/wsd/reference.txt
        final HttpEntity entity =
            MultipartEntityBuilder.create().addPart("data", new FileBody(sourceFile)).build();

        // Use the fluent API to post the file and
        // save the response into the target file.
        final RequestConfig requestConfig = remoteContext.getRequestConfig();
        final URIBuilder uriBuilder = new URIBuilder(buildUrl(requestConfig.getUrl()));

        // We suppose that the server supports custom load properties,
        // but LibreOffice does not support custom load properties,
        // only the sample web service do.
        addPropertiesToBuilder(
            uriBuilder, target.getFormat().getLoadProperties(), LOAD_PROPERTIES_PREFIX_PARAM);

        // We suppose that the server supports custom store properties,
        // but LibreOffice does not support custom store properties,
        // only the sample web service do.
        addPropertiesToBuilder(
            uriBuilder,
            target.getFormat().getStoreProperties(source.getFormat().getInputFamily()),
            STORE_PROPERTIES_PREFIX_PARAM);

        Executor.newInstance(remoteContext.getHttpClient())
            .execute(
                // Request.Post(buildUrl(requestConfig.getUrl()))
                Request.Post(uriBuilder.build())
                    .connectTimeout(requestConfig.getConnectTimeout())
                    .socketTimeout(requestConfig.getSocketTimeout())
                    .body(entity))
            .saveContent(targetFile);

        // onComplete on target will copy the temp file to
        // the OutputStream and then delete the temp file
        // if the output is an OutputStream
        target.onComplete(targetFile);

      } catch (Exception ex) {
        LOGGER.error("Remote conversion failed.", ex);
        final OfficeException officeEx = new OfficeException("Remote conversion failed", ex);
        target.onFailure(targetFile, officeEx);
        throw officeEx;
      }

    } finally {

      // Here the source file is no longer required so we can delete
      // any temporary file that has been created if required.
      source.onConsumed(sourceFile);
    }
  }

  private String buildUrl(final String connectionUrl) {

    // an example URL is like:
    // http://localhost:9980/lool/convert-to/docx

    return StringUtils.appendIfMissing(connectionUrl, "/") + target.getFormat().getExtension();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" + "source=" + source + ", target=" + target + '}';
  }
}
