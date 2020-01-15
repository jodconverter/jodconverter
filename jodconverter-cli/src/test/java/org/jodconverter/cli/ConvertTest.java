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

package org.jodconverter.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.powermock.reflect.Whitebox;

import org.jodconverter.LocalConverter;
import org.jodconverter.cli.util.ConsoleStreamsListenerExtension;
import org.jodconverter.cli.util.ExitException;
import org.jodconverter.cli.util.NoExitExtension;
import org.jodconverter.cli.util.ResetExitExceptionExtension;
import org.jodconverter.cli.util.SystemLogHandler;
import org.jodconverter.office.OfficeManager;

@ExtendWith({
  ConsoleStreamsListenerExtension.class,
  NoExitExtension.class,
  ResetExitExceptionExtension.class
})
public class ConvertTest {

  private static OfficeManager officeManager;

  /** Setup the office manager once before all tests. */
  @BeforeAll
  public static void setUpClass() {
    officeManager = mock(OfficeManager.class);
  }

  @Test
  public void main_WithOptionHelp_PrintHelpAndExitWithCode0() {

    try {
      SystemLogHandler.startCapture();
      Convert.main(new String[] {"-h"});

    } catch (Exception ex) {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .contains("jodconverter-cli [options] infile outfile [infile outfile ...]");
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 0);
    }
  }

  @Test
  public void main_WithOptionHelp_PrintVersionAndExitWithCode0() {

    try {
      SystemLogHandler.startCapture();
      Convert.main(new String[] {"-v"});

    } catch (Exception ex) {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog).contains("jodconverter-cli version");
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 0);
    }
  }

  @Test
  public void main_WithUnknownArgument_PrintErrorHelpAndExitWithCode2() {

    try {
      SystemLogHandler.startCapture();
      Convert.main(new String[] {"-xyz"});

    } catch (Exception ex) {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .contains(
              "Unrecognized option: -xyz",
              "jodconverter-cli [options] infile outfile [infile outfile ...]");
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 2);
    }
  }

  @Test
  public void main_WithMissingsFilenames_PrintErrorHelpAndExitWithCode255() {

    try {
      SystemLogHandler.startCapture();
      Convert.main(new String[] {""});

    } catch (Exception ex) {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .contains("jodconverter-cli [options] infile outfile [infile outfile ...]");
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 255);
    }
  }

  @Test
  public void main_WithWrongFilenamesLength_PrintErrorHelpAndExitWithCode255() {

    try {
      SystemLogHandler.startCapture();
      Convert.main(new String[] {"input1.txt", "output1.pdf", "input2.txt"});

    } catch (Exception ex) {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .contains("jodconverter-cli [options] infile outfile [infile outfile ...]");
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 255);
    }
  }

  @Test
  public void createCliConverter_WithLoadProperties_CreateConverterWithExpectedLoadProperties()
      throws Exception {

    final CommandLine commandLine =
        new DefaultParser()
            .parse(
                (Options) Whitebox.getField(Convert.class, "OPTIONS").get(null),
                new String[] {"-lPassword=myPassword", "output1.pdf", "input2.txt"});

    final CliConverter cliConverter =
        Whitebox.invokeMethod(
            Convert.class, "createCliConverter", commandLine, null, officeManager, null);
    final LocalConverter localConverter =
        (LocalConverter) Whitebox.getField(CliConverter.class, "converter").get(cliConverter);

    final Map<String, Object> expectedLoadProperties =
        new HashMap<>(LocalConverter.DEFAULT_LOAD_PROPERTIES);
    expectedLoadProperties.put("Password", "myPassword");
    assertThat(localConverter).extracting("loadProperties").isEqualTo(expectedLoadProperties);
  }

  @Test
  public void
      createCliConverter_WithFilterDataProperties_CreateConverterWithExpectedStoreProperties()
          throws Exception {

    final CommandLine commandLine =
        new DefaultParser()
            .parse(
                (Options) Whitebox.getField(Convert.class, "OPTIONS").get(null),
                new String[] {"-sFDPageRange=2-2", "output1.pdf", "input2.txt"});

    final CliConverter cliConverter =
        Whitebox.invokeMethod(
            Convert.class, "createCliConverter", commandLine, null, officeManager, null);
    final LocalConverter localConverter =
        (LocalConverter) Whitebox.getField(CliConverter.class, "converter").get(cliConverter);

    final Map<String, Object> expectedFilterData = new HashMap<>();
    expectedFilterData.put("PageRange", "2-2");
    final Map<String, Object> expectedStoreProperties = new HashMap<>();
    expectedStoreProperties.put("FilterData", expectedFilterData);
    assertThat(localConverter).extracting("storeProperties").isEqualTo(expectedStoreProperties);
  }

  @Test
  public void createCliConverter_WithStoreProperties_CreateConverterWithExpectedStoreProperties()
      throws Exception {

    final CommandLine commandLine =
        new DefaultParser()
            .parse(
                (Options) Whitebox.getField(Convert.class, "OPTIONS").get(null),
                new String[] {"-sOverwrite=true", "output1.pdf", "input2.txt"});

    final CliConverter cliConverter =
        Whitebox.invokeMethod(
            Convert.class, "createCliConverter", commandLine, null, officeManager, null);
    final LocalConverter localConverter =
        (LocalConverter) Whitebox.getField(CliConverter.class, "converter").get(cliConverter);

    final Map<String, Object> expectedStoreProperties = new HashMap<>();
    expectedStoreProperties.put("Overwrite", true);
    assertThat(localConverter).extracting("storeProperties").isEqualTo(expectedStoreProperties);
  }

  @Test
  public void
      createCliConverter_WithStoreAndFilterDataProperties_CreateConverterWithExpectedStoreProperties()
          throws Exception {

    final CommandLine commandLine =
        new DefaultParser()
            .parse(
                (Options) Whitebox.getField(Convert.class, "OPTIONS").get(null),
                new String[] {
                  "-sOverwrite=true",
                  "-sFDPageRange=2-4",
                  "-sFDIntProp=5",
                  "output1.pdf",
                  "input2.txt"
                });

    final CliConverter cliConverter =
        Whitebox.invokeMethod(
            Convert.class, "createCliConverter", commandLine, null, officeManager, null);
    final LocalConverter localConverter =
        (LocalConverter) Whitebox.getField(CliConverter.class, "converter").get(cliConverter);

    final Map<String, Object> expectedFilterData = new HashMap<>();
    expectedFilterData.put("PageRange", "2-4");
    expectedFilterData.put("IntProp", 5);
    final Map<String, Object> expectedStoreProperties = new HashMap<>();
    expectedStoreProperties.put("Overwrite", true);
    expectedStoreProperties.put("FilterData", expectedFilterData);
    assertThat(localConverter).extracting("storeProperties").isEqualTo(expectedStoreProperties);
  }
}
