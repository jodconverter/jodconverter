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

package org.jodconverter.remote.task;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.job.SourceDocumentSpecs;
import org.jodconverter.core.task.AbstractOfficeTask;

/**
 * Base class for all remote office tasks implementation.
 *
 * @see org.jodconverter.core.task.OfficeTask
 */
public abstract class AbstractRemoteOfficeTask extends AbstractOfficeTask {

  /**
   * Creates a new task with the specified source document.
   *
   * @param source The source specifications of the document.
   */
  public AbstractRemoteOfficeTask(final @NonNull SourceDocumentSpecs source) {
    super(source);
  }

  @Override
  public @NonNull String toString() {
    return getClass().getSimpleName() + "{" + "source=" + source + '}';
  }
}
