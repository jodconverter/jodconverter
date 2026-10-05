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

package org.jodconverter.core.job;

import java.io.File;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.util.AssertUtils;

/**
 * Base class for all target document specifications implementations.
 *
 * @see TargetDocumentSpecs
 */
public abstract class AbstractTargetDocumentSpecs extends AbstractDocumentSpecs
    implements TargetDocumentSpecs {

  private TargetOptions options;

  protected AbstractTargetDocumentSpecs() {
    super();
  }

  protected AbstractTargetDocumentSpecs(final @NonNull File file) {
    super(file);
  }

  @Override
  public @Nullable TargetOptions getOptions() {
    return options;
  }

  /**
   * Sets the options that apply to the target document.
   *
   * @param options The options to set.
   */
  /* default */ void setOptions(final TargetOptions options) {

    AssertUtils.notNull(options, "options must not be null");
    this.options = options;
  }
}
