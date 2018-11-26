/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

import static org.jodconverter.office.LocalOfficeUtils.property;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;
import com.sun.star.util.XCloseable;

import org.jodconverter.office.utils.Lo;
import org.jodconverter.task.OfficeTask;

public class MockOfficeTask implements OfficeTask {

  private final long delayTime;
  private boolean completed;

  /** Create a new task with default values. */
  public MockOfficeTask() {
    this(0L);
  }

  /**
   * Create a new task with the specified delay.
   *
   * @param delayTime The delay.
   */
  public MockOfficeTask(final long delayTime) {
    super();

    this.delayTime = delayTime;
  }

  @Override
  public void execute(final OfficeContext context) throws OfficeException {

    final LocalOfficeContext ctx = (LocalOfficeContext) context;
    final XComponentLoader loader = ctx.getComponentLoader();
    assert loader != null : "desktop object is null";
    try {
      final PropertyValue[] arguments = new PropertyValue[] {property("Hidden", true)};
      final XComponent document =
          loader.loadComponentFromURL("private:factory/swriter", "_blank", 0, arguments);
      if (delayTime > 0) {
        Thread.sleep(delayTime); // NOSONAR
      }
      Lo.qi(XCloseable.class, document).close(true);
      completed = true;
    } catch (Exception exception) {
      throw new OfficeException("failed to create document", exception);
    }
  }

  /**
   * Gets whether the task is completed or not.
   *
   * @return {@code true} if the task is completed, {@code false} otherwise.
   */
  public boolean isCompleted() {
    return completed;
  }
}
