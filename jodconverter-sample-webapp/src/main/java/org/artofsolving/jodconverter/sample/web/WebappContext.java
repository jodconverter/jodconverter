/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.sample.web;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerBuilder;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;

public class WebappContext {

  public static final String PARAMETER_OFFICE_PORT = "office.port";
  public static final String PARAMETER_OFFICE_HOME = "office.home";
  public static final String PARAMETER_OFFICE_PROFILE = "office.profile";
  public static final String PARAMETER_FILEUPLOAD_FILE_SIZE_MAX = "fileupload.fileSizeMax";

  private static final Logger logger = LoggerFactory.getLogger(WebappContext.class);
  private static final String KEY = WebappContext.class.getName();

  private final ServletFileUpload fileUpload;
  private final OfficeManager officeManager;
  private final OfficeDocumentConverter documentConverter;

  /**
   * Creates a new WebappContext using the specified servlet context.
   *
   * @param servletContext the servlet context that contains properties used to create a JOD
   *     document converter.
   */
  public WebappContext(final ServletContext servletContext) {
    final DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
    final String fileSizeMax = servletContext.getInitParameter(PARAMETER_FILEUPLOAD_FILE_SIZE_MAX);
    fileUpload = new ServletFileUpload(fileItemFactory);
    if (fileSizeMax == null) {
      logger.warn("max file upload size not set");
    } else {
      fileUpload.setFileSizeMax(Integer.parseInt(fileSizeMax));
      logger.info("max file upload size set to {}", fileSizeMax);
    }

    final DefaultOfficeManagerBuilder configuration = new DefaultOfficeManagerBuilder();
    final String officePortParam = servletContext.getInitParameter(PARAMETER_OFFICE_PORT);
    if (officePortParam != null) {
      configuration.setPortNumber(Integer.parseInt(officePortParam));
    }
    final String officeHomeParam = servletContext.getInitParameter(PARAMETER_OFFICE_HOME);
    if (officeHomeParam != null) {
      configuration.setOfficeHome(new File(officeHomeParam));
    }
    final String officeProfileParam = servletContext.getInitParameter(PARAMETER_OFFICE_PROFILE);
    if (officeProfileParam != null) {
      configuration.setTemplateProfileDir(new File(officeProfileParam));
    }

    officeManager = configuration.build();
    documentConverter = new OfficeDocumentConverter(officeManager);
  }

  protected static void init(final ServletContext servletContext) throws OfficeException {
    final WebappContext instance = new WebappContext(servletContext);
    servletContext.setAttribute(KEY, instance);
    instance.officeManager.start();
  }

  protected static void destroy(final ServletContext servletContext) throws OfficeException {
    final WebappContext instance = get(servletContext);
    instance.officeManager.stop();
  }

  /**
   * Gets the WebappContext from the specified servlet context.
   *
   * @param servletContext the servlet context that contains the WebappContext.
   * @return the WebappContext.
   */
  public static WebappContext get(final ServletContext servletContext) {
    return (WebappContext) servletContext.getAttribute(KEY);
  }

  /**
   * Gets the object used to process file uploads.
   *
   * @return a ServletFileUpload.
   */
  public ServletFileUpload getFileUpload() {
    return fileUpload;
  }

  /**
   * Gets the document converter of the context.
   *
   * @return the context's document converter.
   */
  public OfficeManager getOfficeManager() {
    return officeManager;
  }

  /**
   * Gets the document converter of the context.
   *
   * @return the context's document converter.
   */
  public OfficeDocumentConverter getDocumentConverter() {
    return documentConverter;
  }
}
