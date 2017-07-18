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

package org.jodconverter;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.office.DefaultOfficeManager;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;

public abstract class BaseOfficeITest {

  private static OfficeManager officeManager;
  protected static DefaultConverter converter;
  protected static DocumentFormatRegistry formatRegistry;
  protected static final String RESOURCES_DIR = "src/integTest/resources/";
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
    officeManager = DefaultOfficeManager.makeStatic();
    converter = DefaultConverter.make();
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
