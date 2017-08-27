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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.job.SourceDocumentSpecs;
import org.jodconverter.job.TargetDocumentSpecs;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OnlineOfficeContext;

/** Represents the default behavior for an online conversion task. */
public class OnlineConversionTask extends AbstractOnlineOfficeTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(OnlineConversionTask.class);

  private final TargetDocumentSpecs target;

  /**
   * Creates a new conversion task from a specified source to a specified target.
   *
   * @param source The source specifications for the conversion.
   * @param target The target specifications for the conversion.
   */
  public OnlineConversionTask(final SourceDocumentSpecs source, final TargetDocumentSpecs target) {
    super(source);

    this.target = target;
  }

  @Override
  public void execute(final OfficeContext context) throws OfficeException {

    final OnlineOfficeContext onlineContext = (OnlineOfficeContext) context;

    final String url = buildUrl(onlineContext.getConversionUrl());
    try {
      final HttpPost request = new HttpPost(url);

      // see LibreOffice Online API at wsd/reference.txt in source code
      final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.addPart("data", new FileBody(source.getFile()));
      request.setEntity(builder.build());

      final HttpResponse response = onlineContext.getHttpClient().execute(request);

      final int code = response.getStatusLine().getStatusCode();
      if (code >= 300) {
        final OfficeException officeEx =
            new OfficeException(
                "Error while connecting to LibreOffice Online server. Status Code: " + code);
        target.onFailure(target.getFile(), officeEx);
        throw officeEx;
      }

      // Save converted file
      final InputStream fileInputStream = response.getEntity().getContent();
      FileUtils.copyInputStreamToFile(fileInputStream, target.getFile());
      LOGGER.debug("Online conversion task terminated sucessfully.");

    } catch (IOException e) {

      final OfficeException officeEx = new OfficeException("Online conversion task failed", e);
      target.onFailure(target.getFile(), officeEx);
      throw officeEx;
    }
  }

  private String buildUrl(final String connectionUrl) {

    // an example URL is like:
    // http://localhost:9980/lool/convert-to/docx

    return StringUtils.appendIfMissing(connectionUrl, "/") + target.getFormat().getExtension();
  }
}
