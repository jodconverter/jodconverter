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

package org.jodconverter.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jodconverter.local.office.LocalOfficeManager.*;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import org.jodconverter.cli.util.ConsoleStreamsListenerExtension;
import org.jodconverter.cli.util.SystemLogHandler;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.office.LocalOfficeUtils;

/** Contains tests for the {@link Convert} class. */
@ExtendWith(ConsoleStreamsListenerExtension.class)
class ConvertTest {

  @TempDir File testFolder;

  @BeforeEach
  void setUpOfficeHome() {
    System.setProperty("office.home", new File("src/test/resources/oohome").getPath());
  }

  @AfterEach
  void tearDown() {
    System.setProperty("office.home", "");
  }

  @Nested
  class Main {

    @Test
    void withOptionHelp_ShouldPrintHelpAndExitWithCode0() {

      SystemLogHandler.startCapture();
      final var status = Convert.run("-h");
      final var capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .contains("jodconverter-cli [options] infile outfile [infile outfile ...]");
      assertThat(status).isEqualTo(0);
    }

    @Test
    void withOptionHelp_ShouldPrintVersionAndExitWithCode0() {

      SystemLogHandler.startCapture();
      final var status = Convert.run("-v");
      final var capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog).contains("jodconverter-cli version");
      assertThat(status).isEqualTo(0);
    }

    @Test
    void withOptionHelp_ShouldDescribeThePdfOptions() {

      SystemLogHandler.startCapture();
      Convert.run("-h");
      final var capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog).contains("--pdf-preset <name>", "--pdf-option <name=value>");
    }

    @Test
    void withInvalidPdfOption_ShouldPrintErrorAndExitWithCode255() {

      SystemLogHandler.startCapture();
      final var status = Convert.run("--pdf-option", "pages.rang=1", "input.doc", "output.pdf");
      final var capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog).contains("jodconverter-cli: Unknown PDF option 'pages.rang'");
      assertThat(status).isEqualTo(255);
    }

    @Test
    void withInvalidPdfPreset_ShouldPrintErrorAndExitWithCode255() {

      SystemLogHandler.startCapture();
      final var status = Convert.run("--pdf-preset", "tiny", "input.doc", "output.pdf");
      final var capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog).contains("jodconverter-cli: Unknown PDF preset 'tiny'");
      assertThat(status).isEqualTo(255);
    }

    @Test
    void withUnknownArgument_ShouldPrintErrorHelpAndExitWithCode255() {

      SystemLogHandler.startCapture();
      final var status = Convert.run("-yz");
      final var capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .contains(
              "Unrecognized option: -yz",
              "jodconverter-cli [options] infile outfile [infile outfile ...]");
      assertThat(status).isEqualTo(255);
    }

    @Test
    void withMissingsFilenames_ShouldPrintErrorHelpAndExitWithCode255() {

      SystemLogHandler.startCapture();
      final var status = Convert.run("");
      final var capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .contains("jodconverter-cli [options] infile outfile [infile outfile ...]");
      assertThat(status).isEqualTo(255);
    }

    @Test
    void withWrongFilenamesLength_ShouldPrintErrorHelpAndExitWithCode255() {

      SystemLogHandler.startCapture();
      final var status = Convert.run("input1.txt", "output1.pdf", "input2.txt");
      final var capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .contains("jodconverter-cli [options] infile outfile [infile outfile ...]");
      assertThat(status).isEqualTo(255);
    }
  }

  @Nested
  class CreateOfficeManager {

    @Test
    void withUnknownExistingProcessAction_ShouldUseTheDefault() throws Exception {

      final OfficeManager manager =
          ReflectionTestUtils.invokeMethod(
              Convert.class,
              "createOfficeManager",
              Convert.parse("-x", "whatever", "in", "out"),
              null);

      assertThat(manager).isInstanceOf(LocalOfficeManager.class);
    }

    @Test
    void withConnectionUrl_ShouldCreateARemoteOfficeManager() throws Exception {

      final OfficeManager manager =
          ReflectionTestUtils.invokeMethod(
              Convert.class,
              "createOfficeManager",
              Convert.parse("-c", "http://localhost:9980/lool", "in", "out"),
              null);

      assertThat(manager).isInstanceOf(org.jodconverter.remote.office.RemoteOfficeManager.class);
    }

    @Test
    void withDefaultProperties_ShouldCreateManagerWithDefaultProperties() throws Exception {

      final var commandLine = Convert.parse("output1.pdf", "input2.txt");

      final OfficeManager officeManager =
          ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);
      assertThat(officeManager).isInstanceOf(LocalOfficeManager.class);
      assertThat(officeManager)
          .extracting("tempDir")
          .satisfies(
              o ->
                  assertThat(o)
                      .asInstanceOf(InstanceOfAssertFactories.FILE)
                      .hasParent(OfficeUtils.getDefaultWorkingDir()));
      assertThat(officeManager)
          .hasFieldOrPropertyWithValue("taskQueueTimeout", DEFAULT_TASK_QUEUE_TIMEOUT)
          .hasFieldOrPropertyWithValue("taskExecutionTimeout", DEFAULT_TASK_EXECUTION_TIMEOUT)
          .hasFieldOrPropertyWithValue("startFailFast", true);
      assertThat(officeManager)
          .extracting("workers")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .extracting(
                          "maxTasksPerProcess",
                          "officeProcessManager.officeUrl.connectString",
                          "officeProcessManager.officeHome",
                          "officeProcessManager.processManager.class.name",
                          "officeProcessManager.runAsArgs",
                          "officeProcessManager.templateProfileDir",
                          "officeProcessManager.processTimeout",
                          "officeProcessManager.processRetryInterval",
                          "officeProcessManager.afterStartProcessDelay",
                          "officeProcessManager.existingProcessAction",
                          "officeProcessManager.keepAliveOnShutdown",
                          "officeProcessManager.connection.officeUrl.connectString")
                      .containsExactly(
                          DEFAULT_MAX_TASKS_PER_PROCESS,
                          "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                          LocalOfficeUtils.getDefaultOfficeHome(),
                          LocalOfficeUtils.findBestProcessManager().getClass().getName(),
                          Collections.EMPTY_LIST,
                          null,
                          DEFAULT_PROCESS_TIMEOUT,
                          DEFAULT_PROCESS_RETRY_INTERVAL,
                          DEFAULT_AFTER_START_PROCESS_DELAY,
                          DEFAULT_EXISTING_PROCESS_ACTION,
                          DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
                          "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void withCustomValues_ShouldInitializedManagerWithCustomValues() throws Exception {

      final var ooHome = new File(testFolder, "oohomecustom");
      final var program = new File(ooHome, "program");
      program.mkdirs();
      new File(program, "soffice.bin").createNewFile(); // EXECUTABLE_DEFAULT
      new File(program, "soffice").createNewFile(); // EXECUTABLE_MAC
      new File(program, "soffice.exe").createNewFile(); // EXECUTABLE_WINDOWS
      final var macos = new File(ooHome, "MacOS");
      macos.mkdirs();
      new File(macos, "soffice").createNewFile(); // EXECUTABLE_MAC_41
      program.mkdirs();

      final var commandLine =
          Convert.parse(
              "-i",
              ooHome.getPath(),
              "-k",
              "true",
              "-m",
              LocalOfficeUtils.findBestProcessManager().getClass().getName(),
              "-n",
              "localhost",
              "-t",
              "30",
              "-p",
              "2003",
              "-u",
              new File("src/test/resources/templateProfileDir").getPath(),
              "-x",
              ExistingProcessAction.KILL.toString(),
              "input1.txt",
              "output1.pdf");

      final OfficeManager officeManager =
          ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);
      assertThat(officeManager)
          .extracting("tempDir")
          .satisfies(
              o ->
                  assertThat(o)
                      .asInstanceOf(InstanceOfAssertFactories.FILE)
                      .hasParent(OfficeUtils.getDefaultWorkingDir()));
      assertThat(officeManager)
          .hasFieldOrPropertyWithValue("taskQueueTimeout", DEFAULT_TASK_QUEUE_TIMEOUT)
          .hasFieldOrPropertyWithValue("taskExecutionTimeout", 30_000L)
          .hasFieldOrPropertyWithValue("startFailFast", true);
      assertThat(officeManager)
          .extracting("workers")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .extracting(
                          "maxTasksPerProcess",
                          "officeProcessManager.officeUrl.connectString",
                          "officeProcessManager.officeHome",
                          "officeProcessManager.processManager.class.name",
                          "officeProcessManager.runAsArgs",
                          "officeProcessManager.templateProfileDir",
                          "officeProcessManager.processTimeout",
                          "officeProcessManager.processRetryInterval",
                          "officeProcessManager.afterStartProcessDelay",
                          "officeProcessManager.existingProcessAction",
                          "officeProcessManager.keepAliveOnShutdown",
                          "officeProcessManager.connection.officeUrl.connectString")
                      .containsExactly(
                          DEFAULT_MAX_TASKS_PER_PROCESS,
                          "socket,host=localhost,port=2003,tcpNoDelay=1",
                          ooHome,
                          LocalOfficeUtils.findBestProcessManager().getClass().getName(),
                          Collections.EMPTY_LIST,
                          new File("src/test/resources/templateProfileDir"),
                          DEFAULT_PROCESS_TIMEOUT,
                          DEFAULT_PROCESS_RETRY_INTERVAL,
                          DEFAULT_AFTER_START_PROCESS_DELAY,
                          ExistingProcessAction.KILL,
                          true,
                          "socket,host=localhost,port=2003,tcpNoDelay=1"));
    }

    @Test
    void withExistingProcessActionFail_ShouldInitializedManagerWithCustomValues() throws Exception {

      final var commandLine =
          Convert.parse("-x", ExistingProcessAction.FAIL.toString(), "input1.txt", "output1.pdf");

      final OfficeManager officeManager =
          ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);

      assertThat(officeManager)
          .extracting("workers")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .hasFieldOrPropertyWithValue(
                          "officeProcessManager.existingProcessAction",
                          ExistingProcessAction.FAIL));
    }

    @Test
    void withExistingProcessActionConnect_ShouldInitializedManagerWithCustomValues()
        throws Exception {

      final var commandLine =
          Convert.parse(
              "-x", ExistingProcessAction.CONNECT.toString(), "input1.txt", "output1.pdf");

      final OfficeManager officeManager =
          ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);

      assertThat(officeManager)
          .extracting("workers")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .hasFieldOrPropertyWithValue(
                          "officeProcessManager.existingProcessAction",
                          ExistingProcessAction.CONNECT));
    }

    @Test
    void withExistingProcessActionConnectOrKill_ShouldInitializedManagerWithCustomValues()
        throws Exception {

      final var commandLine =
          Convert.parse(
              "-x", ExistingProcessAction.CONNECT_OR_KILL.toString(), "input1.txt", "output1.pdf");

      final OfficeManager officeManager =
          ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);

      assertThat(officeManager)
          .extracting("workers")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .hasFieldOrPropertyWithValue(
                          "officeProcessManager.existingProcessAction",
                          ExistingProcessAction.CONNECT_OR_KILL));
    }
  }

  @Nested
  class CreateCliConverter {

    @Test
    void withLoadProperties_ShouldCreateConverterWithExpectedProperties() throws Exception {

      final var commandLine = Convert.parse("-lPassword=myPassword", "output1.pdf", "input2.txt");

      final OfficeManager officeManager =
          ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);
      Assertions.assertNotNull(officeManager);
      final CliConverter cliConverter =
          ReflectionTestUtils.invokeMethod(
              Convert.class,
              "createCliConverter",
              commandLine,
              null,
              officeManager,
              null,
              Convert.ConversionOptions.parse(commandLine));
      Assertions.assertNotNull(cliConverter);
      final var localConverter =
          (LocalConverter) ReflectionTestUtils.getField(cliConverter, "converter");
      Assertions.assertNotNull(localConverter);

      final var expectedLoadProperties = new HashMap<>(LocalConverter.DEFAULT_LOAD_PROPERTIES);
      expectedLoadProperties.put("Password", "myPassword");
      assertThat(localConverter).extracting("loadProperties").isEqualTo(expectedLoadProperties);
    }

    @Test
    void withFilterDataProperties_ShouldCreateConverterWithExpectedProperties() throws Exception {

      final var commandLine = Convert.parse("-sFDPageRange=2-2", "output1.pdf", "input2.txt");

      final OfficeManager officeManager =
          ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);
      Assertions.assertNotNull(officeManager);
      final CliConverter cliConverter =
          ReflectionTestUtils.invokeMethod(
              Convert.class,
              "createCliConverter",
              commandLine,
              null,
              officeManager,
              null,
              Convert.ConversionOptions.parse(commandLine));
      Assertions.assertNotNull(cliConverter);
      final var localConverter =
          (LocalConverter) ReflectionTestUtils.getField(cliConverter, "converter");
      Assertions.assertNotNull(localConverter);

      final var expectedFilterData = new HashMap<String, Object>();
      expectedFilterData.put("PageRange", "2-2");
      final var expectedStoreProperties = new HashMap<String, Object>();
      expectedStoreProperties.put("FilterData", expectedFilterData);
      assertThat(localConverter).extracting("storeProperties").isEqualTo(expectedStoreProperties);
    }

    @Test
    void withStoreProperties_ShouldCreateConverterWithExpectedProperties() throws Exception {

      final var commandLine = Convert.parse("-sOverwrite=true", "output1.pdf", "input2.txt");

      final OfficeManager officeManager =
          ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);
      Assertions.assertNotNull(officeManager);
      final CliConverter cliConverter =
          ReflectionTestUtils.invokeMethod(
              Convert.class,
              "createCliConverter",
              commandLine,
              null,
              officeManager,
              null,
              Convert.ConversionOptions.parse(commandLine));
      Assertions.assertNotNull(cliConverter);
      final var localConverter =
          (LocalConverter) ReflectionTestUtils.getField(cliConverter, "converter");
      Assertions.assertNotNull(localConverter);

      final var expectedStoreProperties = new HashMap<String, Object>();
      expectedStoreProperties.put("Overwrite", true);
      assertThat(localConverter).extracting("storeProperties").isEqualTo(expectedStoreProperties);
    }

    @Test
    void withStoreAndFilterDataProperties_ShouldCreateConverterWithExpectedProperties()
        throws Exception {

      final var commandLine =
          Convert.parse(
              "-sOverwrite=true",
              "-sReadOnly=false",
              "-sFDPageRange=2-4",
              "-sFDIntProp=5",
              "-sFD=NotFilterData",
              "output1.pdf",
              "input2.txt");

      final OfficeManager officeManager =
          ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);
      Assertions.assertNotNull(officeManager);
      final CliConverter cliConverter =
          ReflectionTestUtils.invokeMethod(
              Convert.class,
              "createCliConverter",
              commandLine,
              null,
              officeManager,
              null,
              Convert.ConversionOptions.parse(commandLine));
      Assertions.assertNotNull(cliConverter);
      final var localConverter =
          (LocalConverter) ReflectionTestUtils.getField(cliConverter, "converter");
      Assertions.assertNotNull(localConverter);

      final var expectedFilterData = new HashMap<String, Object>();
      expectedFilterData.put("PageRange", "2-4");
      expectedFilterData.put("IntProp", 5);
      final var expectedStoreProperties = new HashMap<String, Object>();
      expectedStoreProperties.put("Overwrite", true);
      expectedStoreProperties.put("ReadOnly", false);
      expectedStoreProperties.put("FD", "NotFilterData");
      expectedStoreProperties.put("FilterData", expectedFilterData);
      assertThat(localConverter).extracting("storeProperties").isEqualTo(expectedStoreProperties);
    }

    @Test
    void withValuesContainingEqualSigns_ShouldKeepTheWholeValues() throws Exception {

      final var commandLine =
          Convert.parse(
              "-lPassword=a=b",
              "-s",
              "FDSignCertificateSubjectName=CN=My Company,O=Me",
              "--store-properties",
              "FDSignPDF=true",
              "--store-properties=Base64=YWJj==",
              "output1.pdf",
              "input2.txt");

      final OfficeManager officeManager =
          ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);
      Assertions.assertNotNull(officeManager);
      final CliConverter cliConverter =
          ReflectionTestUtils.invokeMethod(
              Convert.class,
              "createCliConverter",
              commandLine,
              null,
              officeManager,
              null,
              Convert.ConversionOptions.parse(commandLine));
      Assertions.assertNotNull(cliConverter);
      final var localConverter =
          (LocalConverter) ReflectionTestUtils.getField(cliConverter, "converter");
      Assertions.assertNotNull(localConverter);

      final var expectedLoadProperties = new HashMap<>(LocalConverter.DEFAULT_LOAD_PROPERTIES);
      expectedLoadProperties.put("Password", "a=b");
      assertThat(localConverter).extracting("loadProperties").isEqualTo(expectedLoadProperties);

      final var expectedFilterData = new HashMap<String, Object>();
      expectedFilterData.put("SignCertificateSubjectName", "CN=My Company,O=Me");
      expectedFilterData.put("SignPDF", true);
      final var expectedStoreProperties = new HashMap<String, Object>();
      expectedStoreProperties.put("Base64", "YWJj==");
      expectedStoreProperties.put("FilterData", expectedFilterData);
      assertThat(localConverter).extracting("storeProperties").isEqualTo(expectedStoreProperties);

      // The file names are not taken for properties.
      assertThat(commandLine.getArgs()).containsExactly("output1.pdf", "input2.txt");
    }

    @Test
    void withPropertyThatIsNotNameValue_ShouldPrintErrorAndExitWithCode255() {

      SystemLogHandler.startCapture();
      final var loadStatus = Convert.run("-lPassword", "input.doc", "output.pdf");
      final var storeStatus = Convert.run("-s", "=true", "input.doc", "output.pdf");
      final var capturedlog = SystemLogHandler.stopCapture();

      assertThat(capturedlog)
          .contains("jodconverter-cli: Invalid load property 'Password'; expected name=value")
          .contains("jodconverter-cli: Invalid store property '=true'; expected name=value");
      assertThat(loadStatus).isEqualTo(255);
      assertThat(storeStatus).isEqualTo(255);
    }
  }
}
