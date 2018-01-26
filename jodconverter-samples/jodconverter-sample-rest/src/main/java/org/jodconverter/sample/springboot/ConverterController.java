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

package org.jodconverter.sample.springboot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.jodconverter.DocumentConverter;
import org.jodconverter.LocalConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller that will process conversion requests. The mapping is the same as LibreOffice Online
 * (/lool/convert-to) so we can use the jodconverter-online module to send request to this
 * controller. This controller does the same as LibreOffice Online, and also support custom
 * conversions through filters and custom load/store properties.
 */
@Controller
@RequestMapping("/lool/convert-to")
@Api("Conversion Operations which emulate a LibreOffice Online server conversion capabilities.")
public class ConverterController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConverterController.class);

  private final OfficeManager officeManager;
  private final DocumentConverter defaultConverter;

  /**
   * Creates a new controller.
   *
   * @param officeManager The manager used to execute conversions.
   * @param defaultConverter The default converter used to execute conversions.
   */
  public ConverterController(
      final OfficeManager officeManager, final DocumentConverter defaultConverter) {
    super();

    this.officeManager = officeManager;
    this.defaultConverter = defaultConverter;
  }

  @ApiOperation(
      "Convert the incoming document to the specified format (provided as request param) and returns the converted document.")
  @ApiResponses(
    value = {
      @ApiResponse(code = 200, message = "Document converted successfully."),
      @ApiResponse(code = 400, message = "The input document or output format is missing."),
      @ApiResponse(code = 500, message = "An unexpected error occured.")
    }
  )
  @PostMapping(produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public Object convertToUsingParam(
      @ApiParam(value = "The input document to convert.", required = true) @RequestParam("data")
          final MultipartFile inputFile,
      @ApiParam(value = "The document format to convert the input document to.", required = true)
          @RequestParam(name = "format")
          final String convertToFormat,
      @ApiParam(value = "The custom FilterOptions to apply when loading the input document.")
          @RequestParam(name = "loadOptions", required = false)
          final String loadOptions,
      @ApiParam(value = "The custom FilterOptions to apply when storing the output document.")
          @RequestParam(name = "storeOptions", required = false)
          final String storeOptions) {

    LOGGER.debug("convertUsingRequestParam > Converting file to {}", convertToFormat);
    return convert(inputFile, convertToFormat, loadOptions, storeOptions);
  }

  @ApiOperation(
      "Convert the incoming document to the specified format (provided as path param) and returns the converted document.")
  @ApiResponses(
    value = {
      @ApiResponse(code = 200, message = "Document converted successfully."),
      @ApiResponse(code = 400, message = "The input document or output format is missing."),
      @ApiResponse(code = 500, message = "An unexpected error occured.")
    }
  )
  @PostMapping(value = "/{format}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public Object convertToUsingPath(
      @ApiParam(value = "The input document to convert.", required = true) @RequestParam("data")
          final MultipartFile inputFile,
      @ApiParam(value = "The document format to convert the input document to.", required = true)
          @PathVariable(name = "format")
          final String convertToFormat,
      @ApiParam(value = "The custom FilterOptions to apply when loading the input document.")
          @RequestParam(name = "loadOptions", required = false)
          final String loadOptions,
      @ApiParam(value = "The custom FilterOptions to apply when storing the output document.")
          @RequestParam(name = "storeOptions", required = false)
          final String storeOptions) {

    LOGGER.debug("convertUsingPathVariable > Converting file to {}", convertToFormat);
    return convert(inputFile, convertToFormat, loadOptions, storeOptions);
  }

  private ResponseEntity<Object> convert(
      final MultipartFile inputFile,
      final String outputFormat,
      final String loadOptions,
      final String storeOptions) {

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

      final Map<String, Object> loadProperties =
          Optional.ofNullable(loadOptions)
              .filter(StringUtils::isNotBlank)
              .map(
                  options ->
                      Stream.of(options)
                          .collect(
                              Collectors.toMap(opts -> "FilterOptions", opts -> (Object) opts)))
              .orElse(null);

      final Map<String, Object> storeProperties =
          Optional.ofNullable(storeOptions)
              .filter(StringUtils::isNotBlank)
              .map(
                  options ->
                      Stream.of(options)
                          .collect(
                              Collectors.toMap(opts -> "FilterOptions", opts -> (Object) opts)))
              .orElse(null);

      final DocumentConverter converter =
          loadProperties == null && storeProperties == null
              ? defaultConverter
              : LocalConverter.builder()
                  .officeManager(officeManager)
                  .loadProperties(storeProperties)
                  .storeProperties(storeProperties)
                  .build();

      // Convert...
      converter
          .convert(inputFile.getInputStream())
          .as(
              DefaultDocumentFormatRegistry.getFormatByExtension(
                  FilenameUtils.getExtension(inputFile.getOriginalFilename())))
          .to(baos)
          .as(targetFormat)
          .execute();

      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(targetFormat.getMediaType()));
      headers.add(
          "Content-Disposition",
          "attachment; filename="
              + FilenameUtils.getBaseName(inputFile.getOriginalFilename())
              + "."
              + targetFormat.getExtension());
      return ResponseEntity.ok().headers(headers).body(baos.toByteArray());

    } catch (OfficeException | IOException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex);
    }
  }
}
