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

import java.io.File;

import org.junit.Test;

import org.jodconverter.office.DefaultOfficeManager;

public class DefaultConverterTest {

  protected static final String RESOURCES_DIR = "src/test/resources/";
  protected static final String DOCUMENTS_DIR = RESOURCES_DIR + "documents/";

  @Test
  public void testConvertion() throws Exception {

    DefaultOfficeManager officeManager = DefaultOfficeManager.make();
    officeManager.start();
    try {
      DefaultConverter converter = DefaultConverter.make(officeManager);
      converter
          .convert(new File(DOCUMENTS_DIR + "test.doc"))
          .to(new File(DOCUMENTS_DIR + "test.pdf"))
          .execute();

    } finally {
      officeManager.stop();
    }
  }
}
