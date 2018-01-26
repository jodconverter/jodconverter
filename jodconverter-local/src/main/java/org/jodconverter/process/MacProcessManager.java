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

package org.jodconverter.process;

public class MacProcessManager extends UnixProcessManager {

  // This class is required in order to create the default MacProcessManager
  // only on demand, as explained by the Initialization-on-demand holder idiom:
  // https://www.wikiwand.com/en/Initialization-on-demand_holder_idiom
  private static class DefaultHolder { // NOSONAR
    static final MacProcessManager INSTANCE = new MacProcessManager();
  }

  /**
   * Gets the default instance of {@code MacProcessManager}.
   *
   * @return The default {@code MacProcessManager} instance.
   */
  public static MacProcessManager getDefault() {
    return DefaultHolder.INSTANCE;
  }

  @Override
  protected String[] getRunningProcessesCommand(final String process) {

    return new String[] {
      "/bin/bash",
      "-c",
      "/bin/ps -e -o pid,command | /usr/bin/grep " + process + " | /usr/bin/grep -v grep"
    };
  }
}
