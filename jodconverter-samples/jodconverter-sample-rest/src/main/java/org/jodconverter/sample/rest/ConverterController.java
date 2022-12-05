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

package org.jodconverter.sample.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.core.util.StringUtils;
import org.jodconverter.local.LocalConverter;

/**
 * Controller that will process conversion requests. The mapping is the same as LibreOffice Online
 * (/lool/convert-to) so we can use the jodconverter-remote module to send request to this
 * controller. This controller does the same as LibreOffice Online, and also support custom
 * conversions through filters and custom load/store properties.
 */
@Controller
@RequestMapping("/lool/convert-to")
public class ConverterController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConverterController.class);

  private static final String FILTER_DATA = "FilterData";
  private static final String FILTER_DATA_PREFIX_PARAM = "fd";
  private static final String LOAD_PROPERTIES_PREFIX_PARAM = "l";
  private static final String LOAD_FILTER_DATA_PREFIX_PARAM =
      LOAD_PROPERTIES_PREFIX_PARAM + FILTER_DATA_PREFIX_PARAM;
  private static final String STORE_PROPERTIES_PREFIX_PARAM = "s";
  private static final String STORE_FILTER_DATA_PREFIX_PARAM =
      STORE_PROPERTIES_PREFIX_PARAM + FILTER_DATA_PREFIX_PARAM;

  private final OfficeManager officeManager;

  /**
   * Creates a new controller.
   *
   * @param officeManager The manager used to execute conversions.
   */
  public ConverterController(final OfficeManager officeManager) {
    super();

    this.officeManager = officeManager;
  }

  @Operation(
      summary =
          "Converts the incoming document to the specified format (provided as request param)"
              + " and returns the converted document.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Document converted successfully."),
        @ApiResponse(
            responseCode = "400",
            description = "The input document or output format is missing."),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")
      })
  @PostMapping(produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  /* default */ Object convertToUsingParam(
      @Parameter(description = "The input document to convert.", required = true)
          @RequestParam("data")
          final MultipartFile inputFile,
      @Parameter(
              description = "The document format to convert the input document to.",
              required = true)
          @RequestParam(name = "format")
          final String convertToFormat,
      @Parameter(description = "The custom options to apply to the conversion.")
          @RequestParam(required = false)
          final Map<String, String> parameters) {

    LOGGER.debug("convertUsingRequestParam > Converting file to {}", convertToFormat);
    return convert(inputFile, convertToFormat, parameters);
  }

  @Operation(
      summary =
          "Converts the incoming document to the specified format (provided as path param)"
              + " and returns the converted document.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Document converted successfully."),
        @ApiResponse(
            responseCode = "400",
            description = "The input document or output format is missing."),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")
      })
  @PostMapping(value = "/{format}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  /* default */ Object convertToUsingPath(
      @Parameter(description = "The input document to convert.", required = true)
          @RequestParam("data")
          final MultipartFile inputFile,
      @Parameter(
              description = "The document format to convert the input document to.",
              required = true)
          @PathVariable(name = "format")
          final String convertToFormat,
      @Parameter(description = "The custom options to apply to the conversion.")
          @RequestParam(required = false)
          final Map<String, String> parameters) {

    LOGGER.debug("convertUsingPathVariable > Converting file to {}", convertToFormat);
    return convert(inputFile, convertToFormat, parameters);
  }

  private void addProperty(
      final String paramName,
      final Map.Entry<String, String> param,
      final Map<String, Object> properties) {

    final String name = param.getKey().substring(paramName.length());
    final String value = param.getValue();

    if ("true".equalsIgnoreCase(value)) {
      properties.put(name, Boolean.TRUE);
    } else if ("false".equalsIgnoreCase(value)) {
      properties.put(name, Boolean.FALSE);
    } else {
      try {
        final int ival = Integer.parseInt(value);
        properties.put(name, ival);
      } catch (NumberFormatException nfe) {
        properties.put(name, value);
      }
    }
  }

  private void addProperty(
      final String key,
      final String prefix,
      final Map.Entry<String, String> param,
      final Map<String, Object> properties) {
    if (key.startsWith(prefix)) {
      addProperty(key, param, properties);
    }
  }

  private void decodeParameters(
      final Map<String, String> parameters,
      final Map<String, Object> loadProperties,
      final Map<String, Object> storeProperties) {

    if (parameters == null || parameters.isEmpty()) {
      return;
    }

    final Map<String, Object> loadFilterDataProperties = new HashMap<>();
    final Map<String, Object> storeFilterDataProperties = new HashMap<>();
    for (final Map.Entry<String, String> param : parameters.entrySet()) {
      final String key = param.getKey().toLowerCase(Locale.ROOT);
      addProperty(key, LOAD_FILTER_DATA_PREFIX_PARAM, param, loadFilterDataProperties);
      addProperty(key, LOAD_PROPERTIES_PREFIX_PARAM, param, loadProperties);
      addProperty(key, STORE_FILTER_DATA_PREFIX_PARAM, param, storeFilterDataProperties);
      addProperty(key, STORE_PROPERTIES_PREFIX_PARAM, param, storeProperties);
    }

    if (!loadFilterDataProperties.isEmpty()) {
      loadProperties.put(FILTER_DATA, loadFilterDataProperties);
    }

    if (!storeFilterDataProperties.isEmpty()) {
      storeProperties.put(FILTER_DATA, storeFilterDataProperties);
    }
  }

  private ResponseEntity<Object> convert(
      final MultipartFile inputFile,
      final String outputFormat,
      final Map<String, String> parameters) {

    if (inputFile.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    if (StringUtils.isBlank(outputFormat)) {
      return ResponseEntity.badRequest().build();
    }

    // Here, we could have a dedicated service that would convert document
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

      final DocumentFormat targetFormat =
          DefaultDocumentFormatRegistry.getFormatByExtension(outputFormat);
      Assert.notNull(targetFormat, "targetFormat must not be null");

      // Decode the parameters to load and store properties.
      final Map<String, Object> loadProperties =
          new HashMap<>(LocalConverter.DEFAULT_LOAD_PROPERTIES);
      final Map<String, Object> storeProperties = new HashMap<>();
      decodeParameters(parameters, loadProperties, storeProperties);

      // Create a converter with the properties.
      final DocumentConverter converter =
          LocalConverter.builder()
              .officeManager(officeManager)
              .loadProperties(loadProperties)
              .storeProperties(storeProperties)
              .build();

      // Convert...
      converter.convert(inputFile.getInputStream()).to(baos).as(targetFormat).execute();

      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(targetFormat.getMediaType()));
      headers.add(
          "Content-Disposition",
          "attachment; filename="
              + FileUtils.getBaseName(inputFile.getOriginalFilename())
              + "."
              + targetFormat.getExtension());
      return ResponseEntity.ok().headers(headers).body(baos.toByteArray());

    } catch (OfficeException | IOException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex);
    }
  }
}
