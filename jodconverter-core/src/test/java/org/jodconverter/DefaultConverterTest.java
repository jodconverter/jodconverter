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
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;

public class DefaultConverterTest extends BaseOfficeTest {

  protected static final String OUTPUT_DIR =
      "test-output/" + DefaultConverterTest.class.getSimpleName() + "/";

  @Test
  public void convert_FromFileToFile() throws Exception {

    converter
        .convert(new File(DOCUMENTS_DIR + "test.doc"))
        .to(new File(OUTPUT_DIR + "convert_FromFileToFile.pdf"))
        .execute();
  }

  @Test(expected = NullPointerException.class)
  public void convert_FromStreamToFileWithMissingFormat_ShouldThrowNullPointerException()
      throws Exception {

    InputStream inputStream = new FileInputStream(new File(DOCUMENTS_DIR + "test.doc"));
    converter
        .convert(inputStream, null)
        .to(new File(OUTPUT_DIR + "convert_FromStreamToFile.pdf"))
        .execute();
  }

  @Test()
  public void convert_FromStreamToFileWithSupportedFormat_ShouldThrowIllegalArgumentException()
      throws Exception {

    InputStream inputStream = new FileInputStream(new File(DOCUMENTS_DIR + "test.doc"));
    converter
        .convert(inputStream, formatRegistry.getFormatByExtension("doc"))
        .to(new File(OUTPUT_DIR + "convert_FromStreamToFile.pdf"))
        .execute();
  }
}
