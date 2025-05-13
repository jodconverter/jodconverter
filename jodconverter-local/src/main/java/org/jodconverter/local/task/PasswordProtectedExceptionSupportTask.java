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

package org.jodconverter.local.task;

/**
 * Interface that task offering password interaction support that will translate any password
 * interaction into a specific PasswordProtectedException when required.
 */
public interface PasswordProtectedExceptionSupportTask {

  /**
   * Gets whether a password interaction request has been made.
   *
   * @return {@code true} if a password interaction request was made, {@code false} otherwise.
   */
  boolean hasPasswordInteractionRequest();
}
