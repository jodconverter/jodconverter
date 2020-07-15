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

package org.jodconverter.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link IOUtils} class. */
class IOUtilsTest {

  @Test
  void classWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(IOUtils.class);
  }

  @Nested
  class ToString {

    @Test
    void withInputStream_ShouldReturnInputStreamAsString() throws IOException {

      final Charset encoding = StandardCharsets.UTF_8;
      final String test = "ABDCEF\nGHIJKL  \nMNOPQRS\n\tTUVWXYZééé^ç^ç^ç^ç^pawewew";

      try (ByteArrayInputStream in = new ByteArrayInputStream(test.getBytes(encoding))) {
        assertThat(IOUtils.toString(in, encoding)).isEqualTo(test);
      }
    }
  }

  @Nested
  class Copy {

    @Test
    void withInputStream_ShouldReturnOutputStreamWithSameContent() throws IOException {

      final Charset encoding = StandardCharsets.UTF_8;
      final String test = "pç^pçàè^pç^ç;à;èàè.!@#!@#$@#$%ABDCEF\nGHIJKL  \nMNRS\n\tTUVWew";

      try (ByteArrayInputStream in = new ByteArrayInputStream(test.getBytes(encoding));
          ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        IOUtils.copy(in, out);
        assertThat(new String(out.toByteArray(), encoding)).isEqualTo(test);
      }
    }
  }
}
