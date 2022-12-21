/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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

package org.jodconverter.core.office;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link InstalledOfficeManagerHolder} class. */
class InstalledOfficeManagerHolderTest {

  @Test
  void new_ClassWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(InstalledOfficeManagerHolder.class);
  }

  @Nested
  class GetInstance {

    @Test
    void whenInstanceNotSet_ShouldReturnNull() {

      final OfficeManager backup = InstalledOfficeManagerHolder.getInstance();
      try {
        InstalledOfficeManagerHolder.setInstance(null);
        assertThat(InstalledOfficeManagerHolder.getInstance()).isNull();
      } finally {
        InstalledOfficeManagerHolder.setInstance(backup);
      }
    }

    @Test
    void whenInstanceSet_ShouldReturnInstance() {

      final OfficeManager backup = InstalledOfficeManagerHolder.getInstance();
      final OfficeManager newInstance = SimpleOfficeManager.make();
      try {
        InstalledOfficeManagerHolder.setInstance(newInstance);
        assertThat(InstalledOfficeManagerHolder.getInstance()).isEqualTo(newInstance);
      } finally {
        InstalledOfficeManagerHolder.setInstance(backup);
      }
    }
  }

  @Nested
  class SetInstance {

    @Test
    void whenInstanceNotSet_ShouldReturnNull() {

      final OfficeManager backup = InstalledOfficeManagerHolder.getInstance();
      final OfficeManager newInstance = SimpleOfficeManager.make();
      try {
        InstalledOfficeManagerHolder.setInstance(null);
        assertThat(InstalledOfficeManagerHolder.setInstance(newInstance)).isNull();
      } finally {
        InstalledOfficeManagerHolder.setInstance(backup);
      }
    }

    @Test
    void whenInstancetSet_ShouldReturnOldInstacce() {

      final OfficeManager backup = InstalledOfficeManagerHolder.getInstance();
      final OfficeManager newInstance1 = SimpleOfficeManager.make();
      final OfficeManager newInstance2 = SimpleOfficeManager.make();
      try {
        InstalledOfficeManagerHolder.setInstance(newInstance1);
        assertThat(InstalledOfficeManagerHolder.setInstance(newInstance2)).isEqualTo(newInstance1);
      } finally {
        InstalledOfficeManagerHolder.setInstance(backup);
      }
    }
  }
}
