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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.powermock.reflect.Whitebox;

/** Contains tests for the {@link AbstractOfficeManagerPool} class. */
public class AbstractOfficeManagerPoolTest {

  @Test
  public void create_WithCustomConfig_ShouldUseCustomConfig(final @TempDir File testFolder) {

    final SimpleOfficeManagerPoolConfig config =
        new SimpleOfficeManagerPoolConfig(new File(System.getProperty("java.io.tmpdir")));
    config.setWorkingDir(testFolder);
    config.setTaskExecutionTimeout(5_000L);
    config.setTaskQueueTimeout(9_000L);

    final AbstractOfficeManagerPool pool =
        new AbstractOfficeManagerPool(1, config) {
          @Override
          protected OfficeManager[] createPoolEntries() {
            return new OfficeManager[1];
          }
        };

    final SimpleOfficeManagerPoolConfig setupConfig = Whitebox.getInternalState(pool, "config");
    assertThat(setupConfig.getWorkingDir().getPath()).isEqualTo(testFolder.getPath());
    assertThat(setupConfig.getTaskExecutionTimeout()).isEqualTo(5_000L);
    assertThat(setupConfig.getTaskQueueTimeout()).isEqualTo(9_000L);
  }
}
