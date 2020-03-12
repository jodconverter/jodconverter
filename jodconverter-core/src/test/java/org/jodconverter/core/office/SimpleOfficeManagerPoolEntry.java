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

package org.jodconverter.core.office;

import org.jodconverter.core.task.OfficeTask;

/**
 * A SimpleOfficeManagerPoolEntry is responsible to execute tasks submitted through a {@link
 * AbstractOfficeManagerPool} that does not depend on an office installation. It will simply submit
 * tasks with a null office context and wait until the task is done or a configured task execution
 * timeout is reached.
 *
 * @see AbstractOfficeManagerPool
 */
class SimpleOfficeManagerPoolEntry extends AbstractOfficeManagerPoolEntry {

  /**
   * Creates a new pool entry for the specified office URL with the specified configuration.
   *
   * @param taskExecutionTimeout The maximum time allowed to process a task. If the processing time
   *     of a task is longer than this timeout, this task will be aborted and the next task is
   *     processed.
   */
  public SimpleOfficeManagerPoolEntry(final Long taskExecutionTimeout) {
    super(taskExecutionTimeout);
  }

  @Override
  protected void doExecute(final OfficeTask task) throws OfficeException {

    // Simply execute the task
    task.execute(new SimpleOfficeContext());
  }

  @Override
  protected void doStart() {

    setAvailable(true);
  }

  @Override
  protected void doStop() {
    // Nothing to stop here.
  }
}
