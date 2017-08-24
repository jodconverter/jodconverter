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

package org.jodconverter.office;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class OfficeManagerPoolTest {

  protected static final String TEST_OUTPUT_DIR = "build/test-results/";

  private static File outputDir;

  /** Creates an output test directory just once. */
  @BeforeClass
  public static void setUpClass() {

    outputDir = new File(TEST_OUTPUT_DIR, OfficeManagerPoolTest.class.getSimpleName());
    outputDir.mkdirs();
  }

  /** Deletes the output test directory once the tests are all done. */
  @AfterClass
  public static void tearDownClass() {

    FileUtils.deleteQuietly(outputDir);
  }

  @Test
  public void create_WithCustomConfig_ShouldUseCustomConfig() throws Exception {

    final SimpleOfficeManagerPoolConfig config =
        new SimpleOfficeManagerPoolConfig(new File(System.getProperty("java.io.tmpdir")));
    config.setWorkingDir(outputDir);
    config.setTaskExecutionTimeout(5000L);
    config.setTaskQueueTimeout(9000L);

    OfficeManagerPool pool = new OfficeManagerPool(1, config);

    final SimpleOfficeManagerPoolConfig setupConfig =
        (SimpleOfficeManagerPoolConfig) FieldUtils.readField(pool, "config", true);
    assertThat(setupConfig.getWorkingDir().getPath()).isEqualTo(outputDir.getPath());
    assertThat(setupConfig.getTaskExecutionTimeout()).isEqualTo(5000L);
    assertThat(setupConfig.getTaskQueueTimeout()).isEqualTo(9000L);
  }
}
