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

package org.artofsolving.jodconverter;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerBuilder;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;

public abstract class BaseOfficeTest {

  protected static OfficeManager officeManager;
  protected static OfficeDocumentConverter converter;
  protected static DocumentFormatRegistry formatRegistry;
  protected static final String RESOURCES_DIR = "src/test/resources/";
  protected static final String DOCUMENTS_DIR = RESOURCES_DIR + "documents/";
  protected static final String TEST_OUTPUT_DIR = "test-output/";

  /**
   * Starts a default office manager before the execution of the first test in this class.
   *
   * @throws OfficeException if an error occurs.
   */
  @BeforeClass
  public static void startOfficeManager() throws OfficeException {

    // Start an office manager
    officeManager = new DefaultOfficeManagerBuilder().build();
    converter = new OfficeDocumentConverter(officeManager);
    formatRegistry = converter.getFormatRegistry();

    officeManager.start();
  }

  /**
   * Stops the office manager started in the setUpBeforeClass method.
   *
   * @throws OfficeException if an error occurs.
   */
  @AfterClass
  public static void stopOfficeManager() throws OfficeException {

    officeManager.stop();
  }
}
