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

package org.jodconverter.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import org.jodconverter.DocumentConverter;
import org.jodconverter.process.PureJavaProcessManager;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@TestPropertySource(locations = "classpath:config/application-local-purejava.properties")
public class LocalConverterPureJavaITest {

  @Autowired private DocumentConverter converter;

  @Test
  public void testProcessManagerProperty() {

    Object officeManager = ReflectionTestUtils.getField(converter, "officeManager");
    Object poolEntry = ReflectionTestUtils.invokeMethod(officeManager, "acquireManager");
    Object officeProcessManager = ReflectionTestUtils.getField(poolEntry, "officeProcessManager");
    Object config = ReflectionTestUtils.getField(officeProcessManager, "config");
    Object processManager = ReflectionTestUtils.getField(config, "processManager");

    assertThat(processManager).isInstanceOf(PureJavaProcessManager.class);
  }
}
