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

package org.jodconverter.local.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.sun.star.document.UpdateDocMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.job.SourceDocumentSpecsFromFile;
import org.jodconverter.core.job.TargetDocumentSpecsFromFile;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalOfficeManagerExtension;

/** Test the {@link LocalConversionTask} class. */
@ExtendWith(LocalOfficeManagerExtension.class)
public class LocalConvertionTaskITest {

  private static final File SOURCE_FILE = documentFile("test.txt");

  @Test
  public void execute_WithoutCustomLoadProperties_UseDefaultLoadProperties(
      final OfficeManager manager, final @TempDir File testFolder) {

    final SourceDocumentSpecsFromFile source = new SourceDocumentSpecsFromFile(SOURCE_FILE);
    final TargetDocumentSpecsFromFile target =
        new TargetDocumentSpecsFromFile(new File(testFolder, "target.pdf")) {
          @Override
          public DocumentFormat getFormat() {
            return DefaultDocumentFormatRegistry.PDF;
          }
        };
    final LocalConversionTask task =
        new LocalConversionTask(source, target, null, null, null) {
          @Override
          protected Map<String, Object> getLoadProperties() {
            final Map<String, Object> props = super.getLoadProperties();

            assertThat(props)
                .hasSize(3)
                .hasEntrySatisfying("Hidden", o -> assertThat(o).isEqualTo(true))
                .hasEntrySatisfying("ReadOnly", o -> assertThat(o).isEqualTo(true))
                .hasEntrySatisfying(
                    "UpdateDocMode", o -> assertThat(o).isEqualTo(UpdateDocMode.QUIET_UPDATE));

            return props;
          }
        };

    assertThatCode(() -> manager.execute(task)).doesNotThrowAnyException();
  }

  @Test
  public void execute_WithCustomLoadProperties_UseCustomLoadProperties(
      final OfficeManager manager, final @TempDir File testFolder) {

    final SourceDocumentSpecsFromFile source = new SourceDocumentSpecsFromFile(SOURCE_FILE);
    final TargetDocumentSpecsFromFile target =
        new TargetDocumentSpecsFromFile(new File(testFolder, "target.pdf")) {
          @Override
          public DocumentFormat getFormat() {
            return DefaultDocumentFormatRegistry.PDF;
          }
        };
    final Map<String, Object> loadProperties = new HashMap<>();
    loadProperties.put("Hidden", true);
    loadProperties.put("ReadOnly", false);
    loadProperties.put("UpdateDocMode", UpdateDocMode.NO_UPDATE);
    final LocalConversionTask task =
        new LocalConversionTask(source, target, loadProperties, null, null) {

          @Override
          protected Map<String, Object> getLoadProperties() {
            final Map<String, Object> props = super.getLoadProperties();

            assertThat(props)
                .hasSize(3)
                .hasEntrySatisfying("Hidden", o -> assertThat(o).isEqualTo(true))
                .hasEntrySatisfying("ReadOnly", o -> assertThat(o).isEqualTo(false))
                .hasEntrySatisfying(
                    "UpdateDocMode", o -> assertThat(o).isEqualTo(UpdateDocMode.NO_UPDATE));

            return props;
          }
        };

    assertThatCode(() -> manager.execute(task)).doesNotThrowAnyException();
  }

  //  @Test
  //  public void execute_WithCustomLoadProperties_UseCustomLoadProperties(
  //      final OfficeManager manager, final @TempDir File testFolder) throws OfficeException {
  //
  //    final SourceDocumentSpecsFromFile source = new SourceDocumentSpecsFromFile(SOURCE_FILE);
  //    final TargetDocumentSpecsFromFile target =
  //        new TargetDocumentSpecsFromFile(new File(testFolder, "target.pdf")) {
  //          @Override
  //          public DocumentFormat getFormat() {
  //            return DefaultDocumentFormatRegistry.PDF;
  //          }
  //        };
  //    final Map<String, Object> loadProperties = new HashMap<>();
  //    loadProperties.put("Hidden", true);
  //    loadProperties.put("ReadOnly", false);
  //    loadProperties.put("UpdateDocMode", UpdateDocMode.NO_UPDATE);
  //    final LocalConversionTask task =
  //        new LocalConversionTask(source, target, loadProperties, null, null) {
  //          @Override
  //          protected Map<String, Object> getLoadProperties() {
  //            final Map<String, Object> props = super.getLoadProperties();
  //
  //            assertThat(props)
  //                .hasSize(3)
  //                .hasEntrySatisfying("Hidden", o -> assertThat(o).isEqualTo(true))
  //                .hasEntrySatisfying("ReadOnly", o -> assertThat(o).isEqualTo(false))
  //                .hasEntrySatisfying(
  //                    "UpdateDocMode", o -> assertThat(o).isEqualTo(UpdateDocMode.NO_UPDATE));
  //
  //            return props;
  //          }
  //        };
  //
  //    assertThatCode(() -> manager.execute(task)).doesNotThrowAnyException();
  //  }
}
