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

package org.jodconverter.process;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;

import org.jodconverter.office.DefaultOfficeManager;

public class ProcessManagerITest {

  /**
   * Tests that using an custom process manager that appears in the classpath will be used.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void customProcessManager() throws Exception {

    final DefaultOfficeManager officeManager =
        DefaultOfficeManager.builder()
            .processManager("org.jodconverter.process.CustomProcessManager")
            .build();

    final Object config = FieldUtils.readField(officeManager, "config", true);
    final ProcessManager manager =
        (ProcessManager) FieldUtils.readField(config, "processManager", true);
    assertTrue(manager instanceof CustomProcessManager);
  }
}
