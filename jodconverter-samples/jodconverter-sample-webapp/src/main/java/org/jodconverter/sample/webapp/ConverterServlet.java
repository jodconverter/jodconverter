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

package org.jodconverter.sample.webapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.DocumentConverter;

public class ConverterServlet extends HttpServlet {
  private static final long serialVersionUID = -591469426224201748L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ConverterServlet.class);

  @Override
  public void init() throws ServletException {
    LOGGER.info("Servlet {} has started", this.getServletName());
  }

  @Override
  public void destroy() {
    LOGGER.info("Servlet {} has stopped", this.getServletName());
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {

    if (!ServletFileUpload.isMultipartContent(request)) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only multipart requests are allowed");
      return;
    }

    final WebappContext webappContext = WebappContext.get(getServletContext());

    final FileItem uploadedFile;
    try {
      uploadedFile = getUploadedFile(webappContext.getFileUpload(), request);
    } catch (FileUploadException fileUploadException) {
      throw new ServletException(fileUploadException);
    }
    if (uploadedFile == null) {
      throw new ServletException("Uploaded file is null");
    }
    final String inputExtension = FilenameUtils.getExtension(uploadedFile.getName());

    final String baseName = FilenameUtils.getBaseName(uploadedFile.getName());
    final File inputFile = File.createTempFile(baseName, "." + inputExtension);
    writeUploadedFile(uploadedFile, inputFile);

    final String outputExtension = FilenameUtils.getExtension(request.getRequestURI());
    final File outputFile = File.createTempFile(baseName, "." + outputExtension);
    try {
      final DocumentConverter converter = webappContext.getDocumentConverter();
      final long startTime = System.currentTimeMillis();
      converter.convert(inputFile).to(outputFile).execute();
      LOGGER.info(
          String.format(
              "Successful conversion: %s [%db] to %s in %dms",
              inputExtension,
              inputFile.length(),
              outputExtension,
              System.currentTimeMillis() - startTime));
      response.setContentType(
          converter.getFormatRegistry().getFormatByExtension(outputExtension).getMediaType());
      response.setHeader(
          "Content-Disposition", "attachment; filename=" + baseName + "." + outputExtension);
      sendFile(outputFile, response);
    } catch (Exception exception) {
      LOGGER.error(
          String.format(
              "Failed conversion: %s [%db] to %s; %s; input file: %s",
              inputExtension, inputFile.length(), outputExtension, exception, inputFile.getName()));
      throw new ServletException("Conversion failed", exception);
    } finally {
      FileUtils.deleteQuietly(outputFile);
      FileUtils.deleteQuietly(inputFile);
    }
  }

  private void sendFile(final File file, final HttpServletResponse response) throws IOException {

    response.setContentLength((int) file.length());
    try (InputStream inputStream = new FileInputStream(file)) {
      IOUtils.copy(inputStream, response.getOutputStream());
    }
  }

  private void writeUploadedFile(final FileItem uploadedFile, final File destinationFile)
      throws ServletException {

    try {
      uploadedFile.write(destinationFile);
    } catch (Exception exception) {
      throw new ServletException("Error writing uploaded file", exception);
    }
    uploadedFile.delete();
  }

  private FileItem getUploadedFile(
      final ServletFileUpload fileUpload, final HttpServletRequest request)
      throws FileUploadException {

    return fileUpload
        .parseRequest(request)
        .stream()
        .filter(fileItem -> !fileItem.isFormField())
        .findFirst()
        .orElse(null);
  }
}
