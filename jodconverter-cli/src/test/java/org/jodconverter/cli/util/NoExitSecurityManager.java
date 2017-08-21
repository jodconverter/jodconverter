/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

package org.jodconverter.cli.util;

import java.security.Permission;

/**
 * Helper class while testing that will prevent a call to System.exit to actually shutdown the VM.
 * Instead, a ExitException is thrown.
 */
public class NoExitSecurityManager extends SecurityManager {

  @Override
  public void checkExit(final int status) {
    super.checkExit(status);

    ExitException.INSTANCE.setStatus(status);
    throw ExitException.INSTANCE;
  }

  @Override
  public void checkPermission(final Permission perm) {
    // allow anything.
  }

  @Override
  public void checkPermission(final Permission perm, final Object context) {
    // allow anything.
  }
}
