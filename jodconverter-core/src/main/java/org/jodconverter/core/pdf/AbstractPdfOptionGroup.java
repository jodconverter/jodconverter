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

package org.jodconverter.core.pdf;

import java.util.Map;

import org.jodconverter.core.util.AssertUtils;

/** Base class of the groups of options of a {@link PdfOptions.Builder}. */
abstract class AbstractPdfOptionGroup {

  private final Map<PdfOption, Object> values;

  /* default */ AbstractPdfOptionGroup(final Map<PdfOption, Object> values) {
    this.values = values;
  }

  // Sets the value of an option, or removes the option if the value is null.
  /* default */
  final void set(final PdfOption option, final Object value) {
    if (value == null) {
      values.remove(option);
    } else {
      values.put(option, value);
    }
  }

  // Sets a text option, or removes it if the text is null.
  /* default */
  final void setText(final PdfOption option, final String text) {
    if (text != null) {
      AssertUtils.notBlank(text, option.getFilterDataName() + " must not be blank");
    }
    set(option, text);
  }

  // Sets a number option that must not be lower than a minimum.
  /* default */
  final void setAtLeast(
      final PdfOption option, final String name, final int value, final int minimum) {
    AssertUtils.isTrue(value >= minimum, name + " must be at least " + minimum);
    set(option, value);
  }
}
