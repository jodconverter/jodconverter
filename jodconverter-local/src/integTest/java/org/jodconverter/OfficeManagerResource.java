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

package org.jodconverter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.office.LocalOfficeManager;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;

/**
 * Class rule used providing a single OfficeManager instance that will be used for most of the
 * integration tests.
 */
public final class OfficeManagerResource extends ExternalResource {

  public static final TestRule INSTANCE = new OfficeManagerResource();
  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeManagerResource.class);

  /* default */ OfficeManager officeManager;
  private final AtomicBoolean started = new AtomicBoolean();
  private final AtomicInteger counter = new AtomicInteger(0);

  // Private ctor.
  private OfficeManagerResource() {
    super();
  }

  @Override
  protected void before() throws Throwable {

    counter.incrementAndGet();
    if (!started.compareAndSet(false, true)) {
      return;
    }

    // Start the office manager and install it. Don't use the default port
    // number here in order to be able to use it in other tests.
    officeManager = LocalOfficeManager.builder().portNumbers(2010).install().build();
    officeManager.start();

    // Ensure we stop the manager when the VM shutdown
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                try {
                  officeManager.stop();
                } catch (OfficeException ex) {
                  LOGGER.error("Unable to stop the office manager.", ex);
                }
              }
            });
  }
}
