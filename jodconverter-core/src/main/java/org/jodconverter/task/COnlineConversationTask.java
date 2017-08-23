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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.filter.FilterChain;
import org.jodconverter.job.SourceDocumentSpecs;
import org.jodconverter.job.TargetDocumentSpecs;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeTask;

public class COnlineConversationTask implements OfficeTask {
  private static final Logger logger = LoggerFactory.getLogger(DefaultConversionTask.class);

  private final SourceDocumentSpecs source;
  private final TargetDocumentSpecs target;
  private final String connectionURL;
  private Map<String, Object> defaultLoadProperties;
  private FilterChain filterChain;

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
  public COnlineConversationTask(
      final SourceDocumentSpecs source,
      final TargetDocumentSpecs target,
      final Map<String, Object> defaultLoadProperties,
      final FilterChain filterChain,
      final String connectionURL) {
    super();

    this.source = source;
    this.target = target;
    this.connectionURL = connectionURL;
    this.defaultLoadProperties = defaultLoadProperties;
    this.filterChain = filterChain;
  }

  @Override
  public void execute(OfficeContext context) throws OfficeException {
    try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

      // TODO set url / choose URL
      StringBuilder urlBuilder = new StringBuilder(connectionURL);

      try {
        ensureURL(urlBuilder);
      } catch (MalformedURLException e) {
        OfficeException officeEx = new OfficeException("Malformed URL", e);
        target.onFailure(target.getFile(), officeEx);
        throw officeEx;
      }

      HttpPost request = new HttpPost(urlBuilder.toString());

      // see LibreOffice Online API at wsd/reference.txt in source code
      MultipartEntity entity = new MultipartEntity();
      entity.addPart("data", new FileBody(source.getFile()));
      request.setEntity(entity);

      HttpResponse response = httpClient.execute(request);

      final int code = response.getStatusLine().getStatusCode();
      if (code >= 300) {
        logger.error("Error while connecting to LibreOffice Online server");
        return;
      }

      // save converted file
      InputStream fileInputStream = response.getEntity().getContent();
      FileUtils.copyInputStreamToFile(fileInputStream, target.getFile());
      logger.debug("converted file saved");

    } catch (IOException e) {

      OfficeException officeEx = new OfficeException("Conversion failed", e);
      target.onFailure(target.getFile(), officeEx);
      throw officeEx;
    }
  }

  private void ensureURL(StringBuilder urlBuilder) throws MalformedURLException {
    // an example URL is like:
    // http://localhost:9980/lool/convert-to/docx

    URL url = new URL(urlBuilder.toString());
    String path = url.toString();

    if (!path.contains("lool/convert-to")) {
      // assume user did not use full path
      if (!path.endsWith("/")) urlBuilder.append('/');
      urlBuilder.append("lool/convert-to/");
      urlBuilder.append(target.getFormat().getExtension()); // e.g. docx
    }
  }
}
