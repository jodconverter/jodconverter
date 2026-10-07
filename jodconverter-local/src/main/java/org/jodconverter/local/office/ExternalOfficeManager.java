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

package org.jodconverter.local.office;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * The former name of {@link AttachedOfficeManager}, the manager that attaches to office processes
 * it does not start. The factory methods of this class give an {@code AttachedOfficeManager}.
 *
 * @deprecated Use {@link AttachedOfficeManager}; this class will be removed in a later release.
 */
@Deprecated(since = "5.0", forRemoval = true)
public final class ExternalOfficeManager {

  private static final String SINCE = "5.0";

  /** The default host name of the office processes. */
  public static final String DEFAULT_HOSTNAME = AttachedOfficeManager.DEFAULT_HOSTNAME;

  /** The default value for connecting when the manager starts. */
  public static final boolean DEFAULT_CONNECT_ON_START =
      AttachedOfficeManager.DEFAULT_CONNECT_ON_START;

  /** The default timeout when connecting to an office process, in milliseconds (2 minutes). */
  public static final long DEFAULT_CONNECT_TIMEOUT = AttachedOfficeManager.DEFAULT_CONNECT_TIMEOUT;

  /** The default delay between two connection attempts, in milliseconds. */
  public static final long DEFAULT_CONNECT_RETRY_INTERVAL =
      AttachedOfficeManager.DEFAULT_CONNECT_RETRY_INTERVAL;

  /** The default "fail fast" behavior when a connection attempt is made. */
  public static final boolean DEFAULT_CONNECT_FAIL_FAST =
      AttachedOfficeManager.DEFAULT_CONNECT_FAIL_FAST;

  /** The default maximum number of tasks a connection can execute before reconnecting. */
  public static final int DEFAULT_MAX_TASKS_PER_CONNECTION =
      AttachedOfficeManager.DEFAULT_MAX_TASKS_PER_CONNECTION;

  // Suppresses default constructor, ensuring non-instantiability.
  private ExternalOfficeManager() {
    throw new AssertionError("Utility class must not be instantiated");
  }

  /**
   * Creates a new builder of an {@link AttachedOfficeManager}.
   *
   * @return A new builder instance.
   * @deprecated Use {@link AttachedOfficeManager#builder()}.
   */
  @Deprecated(since = SINCE, forRemoval = true)
  public static AttachedOfficeManager.@NonNull Builder builder() {
    return AttachedOfficeManager.builder();
  }

  /**
   * Creates a new {@link AttachedOfficeManager} with default configuration.
   *
   * @return An {@link AttachedOfficeManager} with default configuration.
   * @deprecated Use {@link AttachedOfficeManager#make()}.
   */
  @Deprecated(since = SINCE, forRemoval = true)
  public static @NonNull AttachedOfficeManager make() {
    return AttachedOfficeManager.make();
  }

  /**
   * Creates a new {@link AttachedOfficeManager} with default configuration, installed as the unique
   * instance of the {@link org.jodconverter.core.office.InstalledOfficeManagerHolder}.
   *
   * @return An {@link AttachedOfficeManager} with default configuration.
   * @deprecated Use {@link AttachedOfficeManager#install()}.
   */
  @Deprecated(since = SINCE, forRemoval = true)
  public static @NonNull AttachedOfficeManager install() {
    return AttachedOfficeManager.install();
  }
}
