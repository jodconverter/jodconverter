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

package org.jodconverter.local.office;

/**
 * Represents the actions that can be taken by an {@link LocalOfficeProcessManager} when trying to
 * start an office process with a connection string and that there already is a process running with
 * the same connection string.
 */
public enum ExistingProcessAction {

  /**
   * Indicates that the {@link LocalOfficeProcessManager} must fail when trying to start an office
   * process and there already is a process running with the same connection string. If that is the
   * case, a {@link org.jodconverter.core.office.OfficeException} is thrown.
   */
  FAIL,

  /**
   * Indicates that the manager must kill the existing office process when starting a new office
   * process and there already is a process running with the same connection string.
   */
  KILL,

  /**
   * Indicates that the manager must connect to the existing office process when starting a new
   * office process and there already is a process running with the same connection string.
   */
  CONNECT,

  /**
   * Indicates that the manager must first try to connect to the existing office process when
   * starting a new office process and there already is a process running with the same connection
   * string. If the connection fails, then the manager must kill the existing office process.
   */
  CONNECT_OR_KILL
}
