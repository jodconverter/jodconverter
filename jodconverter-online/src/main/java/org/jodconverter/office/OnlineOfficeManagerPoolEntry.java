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

package org.jodconverter.office;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import org.jodconverter.task.OfficeTask;

/**
 * A OnlineOfficeManagerPoolEntry is responsible to execute tasks submitted through a {@link
 * OnlineOfficeManager} that does not depend on an office installation. It will send conversion
 * request to a LibreOffice Online server and wait until the task is done or a configured task
 * execution timeout is reached.
 *
 * @see OnlineOfficeManager
 */
class OnlineOfficeManagerPoolEntry extends AbstractOfficeManagerPoolEntry {

  final String connectionUrl;

  /**
   * Creates a new pool entry with the specified configuration.
   *
   * @param connectionUrl The URL to the LibreOffice Online server.
   * @param config The entry configuration.
   */
  public OnlineOfficeManagerPoolEntry(
      final String connectionUrl, final OnlineOfficeManagerPoolEntryConfig config) {
    super(config);

    this.connectionUrl = connectionUrl;
  }

  @Override
  protected void doStart() throws OfficeException {

    taskExecutor.setAvailable(true);
  }

  @Override
  protected void doExecute(final OfficeTask task) throws OfficeException {

    try (final CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
      final String conversionUrl = buildUrl(connectionUrl);
      task.execute(
          // TODO: Create a dedicated class for an OnlineOfficeContext
          new OnlineOfficeContext() {
            @Override
            public HttpClient getHttpClient() {
              return httpClient;
            }

            @Override
            public String getConversionUrl() {
              return conversionUrl;
            }
          });
    } catch (IOException ex) {
      throw new OfficeException("Unable to create the HTTP client", ex);
    }
  }

  @Override
  protected void doStop() throws OfficeException {
    // Nothing to stop here.
  }

  private String buildUrl(final String connectionUrl) throws MalformedURLException {

    // an example URL is like:
    // http://localhost:9980/lool/convert-to/docx

    final URL url = new URL(connectionUrl);
    final String path = url.toExternalForm().toLowerCase();
    if (StringUtils.endsWithAny(path, "lool/convert-to", "lool/convert-to/")) {
      return StringUtils.appendIfMissing(connectionUrl, "/");
    } else if (StringUtils.endsWith(path, "/")) {
      return connectionUrl + "lool/convert-to/";
    } else {
      return connectionUrl + "/lool/convert-to/";
    }
  }
}
