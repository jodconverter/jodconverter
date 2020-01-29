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

package org.jodconverter.sample.springboot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;

/** Controller providing conversion endpoints. */
@Controller
public class ConverterController {

  private static final String ATTRNAME_ERROR_MESSAGE = "errorMessage";
  private static final String ON_ERROR_REDIRECT = "redirect:/";

  @Autowired private DocumentConverter converter;

  @GetMapping("/")
  /* default */ String index() {
    return "converter";
  }

  /**
   * Converts a souirce file to a target format.
   *
   * @param inputFile Source file to convert.
   * @param outputFormat Output format of the conversion.
   * @param redirectAttributes Model that contains attributes
   * @return The converted file, or the error redirection if an error occurs.
   */
  @PostMapping("/converter")
  /* default */ Object convert(
      @RequestParam("inputFile") final MultipartFile inputFile,
      @RequestParam(name = "outputFormat", required = false) final String outputFormat,
      final RedirectAttributes redirectAttributes) {

    if (inputFile.isEmpty()) {
      redirectAttributes.addFlashAttribute(
          ATTRNAME_ERROR_MESSAGE, "Please select a file to upload.");
      return ON_ERROR_REDIRECT;
    }

    if (StringUtils.isBlank(outputFormat)) {
      redirectAttributes.addFlashAttribute(
          ATTRNAME_ERROR_MESSAGE, "Please select an output format.");
      return ON_ERROR_REDIRECT;
    }

    // Here, we could have a dedicated service that would convert document
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

      final DocumentFormat targetFormat =
          DefaultDocumentFormatRegistry.getFormatByExtension(outputFormat);
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
      return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);

    } catch (OfficeException | IOException e) {
      redirectAttributes.addFlashAttribute(
          ATTRNAME_ERROR_MESSAGE,
          "Unable to convert the file "
              + inputFile.getOriginalFilename()
              + ". Cause: "
              + e.getMessage());
    }

    return ON_ERROR_REDIRECT;
  }
}
