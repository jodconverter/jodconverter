/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.office;

import static org.artofsolving.jodconverter.office.OfficeUtils.property;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XCloseable;

public class MockOfficeTask implements OfficeTask {

  private long delayTime;
  private boolean completed;

  public MockOfficeTask() {
    // default
  }

  public MockOfficeTask(final long delayTime) {
    this.delayTime = delayTime;
  }

  @Override
  public void execute(final OfficeContext context) throws OfficeException {

    final XComponentLoader loader = context.getComponentLoader();
    assert loader != null : "desktop object is null";
    try {
      final PropertyValue[] arguments = new PropertyValue[] {property("Hidden", true)};
      final XComponent document =
          loader.loadComponentFromURL("private:factory/swriter", "_blank", 0, arguments);
      if (delayTime > 0) {
        Thread.sleep(delayTime); // NOSONAR
      }
      UnoRuntime.queryInterface(XCloseable.class, document).close(true);
      completed = true;
    } catch (Exception exception) {
      throw new OfficeException("failed to create document", exception);
    }
  }

  /**
   * Gets whether the task is completed or not.
   *
   * @return true if the task is completed, false otherwise.
   */
  public boolean isCompleted() {
    return completed;
  }
}
