/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.job.SourceDocumentSpecs;
import org.jodconverter.core.job.TargetDocumentSpecs;
import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.remote.office.RemoteOfficeContext;

/**
 * Represents the default behavior for a remote conversion task: the source document is posted to
 * the conversion service of a LibreOffice Online server, as a multipart form, and the response is
 * the converted document.
 */
public class RemoteConversionTask extends AbstractRemoteOfficeTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConversionTask.class);

  private static final String FILTER_DATA = "FilterData";
  private static final String FILTER_DATA_PREFIX_PARAM = "fd";
  private static final String LOAD_PROPERTIES_PREFIX_PARAM = "l";
  private static final String STORE_PROPERTIES_PREFIX_PARAM = "s";
  private static final String CRLF = "\r\n";
  private static final int MAX_ERROR_BODY_LENGTH = 1_000;

  private final TargetDocumentSpecs target;
  // The boundary of the multipart body, the same for the header and the body of one request.
  private String boundary;

  /**
   * Creates a new conversion task from a specified source to a specified target.
   *
   * @param source The source specifications of the conversion.
   * @param target The target specifications of the conversion.
   */
  public RemoteConversionTask(
      final @NonNull SourceDocumentSpecs source, final @NonNull TargetDocumentSpecs target) {
    super(source);

    this.target = target;
  }

  private static void addProperties(
      final List<String> parameters,
      final String parameterPrefix,
      final Map<String, Object> properties) {

    if (properties == null) {
      return;
    }
    for (final var entry : properties.entrySet()) {
      final var key = entry.getKey();
      final var value = entry.getValue();
      // First, check if we are dealing with the FilterData property
      if (FILTER_DATA.equalsIgnoreCase(key) && value instanceof Map<?, ?> filterData) {
        // Add all the FilterData properties
        for (final var fdentry : filterData.entrySet()) {
          addParameter(
              parameters,
              parameterPrefix + FILTER_DATA_PREFIX_PARAM + fdentry.getKey(),
              fdentry.getValue().toString());
        }
      } else {
        addParameter(parameters, parameterPrefix + key, value.toString());
      }
    }
  }

  private static void addParameter(
      final List<String> parameters, final String name, final String value) {
    parameters.add(encode(name) + "=" + encode(value));
  }

  // Encodes a query parameter, with spaces as %20 rather than as the + of a form.
  private static String encode(final String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }

  @Override
  public void execute(final @NonNull OfficeContext context) throws OfficeException {

    LOGGER.debug("Executing remote conversion task...");
    final var remoteContext = (RemoteOfficeContext) context;

    // Obtain a source file that can be loaded by office. If the source
    // is an input stream, then a temporary file will be created from the
    // stream. The temporary file will be deleted once the task is done.
    final var sourceFile = source.getFile();
    try {

      // Get the target file (which is a temporary file if the
      // output target is an output stream).
      final var targetFile = target.getFile();

      try {
        final var requestConfig = remoteContext.getRequestConfig();
        final var request =
            HttpRequest.newBuilder(buildUri(requestConfig.url()))
                .timeout(Duration.ofMillis(Math.max(requestConfig.socketTimeout(), 1L)))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary())
                .POST(multipartBody(sourceFile))
                .build();
        final var response = remoteContext.send(request, saveOrReadError(targetFile));
        if (response.body().isPresent()) {
          throw new IOException(
              String.format(
                  "The server answered with the status %d: %s",
                  response.statusCode(), response.body().get()));
        }

        // onComplete on target will copy the temp file to
        // the OutputStream and then delete the temp file
        // if the output is an OutputStream
        target.onComplete(targetFile);

      } catch (Exception ex) {
        final var officeEx = new OfficeException("Remote conversion failed", ex);
        target.onFailure(targetFile, officeEx);
        throw officeEx;
      }

    } finally {
      // Here the source file is no longer required, so we can delete
      // any temporary file that has been created if required.
      source.onConsumed(sourceFile);
    }
  }

  private String boundary() {
    if (boundary == null) {
      boundary = "----JODConverter" + UUID.randomUUID().toString().replace("-", "");
    }
    return boundary;
  }

  // The source document, as the "data" part of a multipart form.
  private HttpRequest.BodyPublisher multipartBody(final File sourceFile) {

    final var head =
        ("--"
                + boundary()
                + CRLF
                + "Content-Disposition: form-data; name=\"data\"; filename=\""
                + sourceFile.getName().replace("\"", "%22")
                + "\""
                + CRLF
                + "Content-Type: application/octet-stream"
                + CRLF
                + CRLF)
            .getBytes(StandardCharsets.UTF_8);
    final var tail = (CRLF + "--" + boundary() + "--" + CRLF).getBytes(StandardCharsets.UTF_8);
    return HttpRequest.BodyPublishers.ofInputStream(
        () -> {
          try {
            final List<InputStream> parts =
                List.of(
                    new ByteArrayInputStream(head),
                    Files.newInputStream(sourceFile.toPath()),
                    new ByteArrayInputStream(tail));
            return new SequenceInputStream(Collections.enumeration(parts));
          } catch (IOException ex) {
            throw new UncheckedIOException(ex);
          }
        });
  }

  // Saves the body of a successful response into the target file, and gives the body of a failed
  // response as the error message.
  private static HttpResponse.BodyHandler<Optional<String>> saveOrReadError(final File targetFile) {

    return info ->
        info.statusCode() / 100 == 2
            ? HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofFile(targetFile.toPath()), path -> Optional.empty())
            : HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8),
                body ->
                    Optional.of(
                        body.length() > MAX_ERROR_BODY_LENGTH
                            ? body.substring(0, MAX_ERROR_BODY_LENGTH) + "..."
                            : body));
  }

  // The URI of the conversion: the service URL, the target extension, and the properties as
  // query parameters.
  private URI buildUri(final String serviceUrl) {

    // We suppose that the server supports custom load properties, but LibreOffice Online
    // does not support custom load properties, only the sample web service do.
    // Load properties are used to load the source document, so they come from its format.
    final var parameters = new ArrayList<String>();
    Optional.ofNullable(source.getFormat())
        .map(DocumentFormat::getLoadProperties)
        .ifPresent(
            loadProperties ->
                addProperties(parameters, LOAD_PROPERTIES_PREFIX_PARAM, loadProperties));

    // We suppose that the server supports custom store properties, but LibreOffice Online
    // does not support custom store properties, only the sample web service do.
    // The options of this conversion take precedence over the properties of the target format.
    final var storeProperties = new LinkedHashMap<String, Object>();
    Optional.ofNullable(source.getFormat())
        .map(DocumentFormat::getInputFamily)
        .map(family -> Objects.requireNonNull(target.getFormat()).getStoreProperties(family))
        .ifPresent(storeProperties::putAll);
    Optional.ofNullable(target.getOptions()).ifPresent(options -> options.applyTo(storeProperties));
    addProperties(parameters, STORE_PROPERTIES_PREFIX_PARAM, storeProperties);

    // An example URL is like: http://localhost:9980/lool/convert-to/docx
    final var url = serviceUrl + Objects.requireNonNull(target.getFormat()).getExtension();
    return URI.create(parameters.isEmpty() ? url : url + "?" + String.join("&", parameters));
  }

  @Override
  public @NonNull String toString() {
    return getClass().getSimpleName() + "{" + "source=" + source + ", target=" + target + '}';
  }
}
