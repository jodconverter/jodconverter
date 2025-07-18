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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.jodconverter.cli.util.*;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.office.LocalOfficeUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_EXECUTION_TIMEOUT;
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_QUEUE_TIMEOUT;
import static org.jodconverter.local.office.LocalOfficeManager.*;

/**
 * Contains tests for the {@link Convert} class.
 */
@ExtendWith({
        ConsoleStreamsListenerExtension.class,
        NoExitExtension.class,
        ResetExitExceptionExtension.class
})
class ConvertTest {

    @TempDir
    File testFolder;

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

            try {
                SystemLogHandler.startCapture();
                Convert.main(new String[]{"-h"});

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
        void withOptionHelp_ShouldPrintVersionAndExitWithCode0() {

            try {
                SystemLogHandler.startCapture();
                Convert.main(new String[]{"-v"});

            } catch (Exception ex) {
                final String capturedlog = SystemLogHandler.stopCapture();
                assertThat(capturedlog).contains("jodconverter-cli version");
                assertThat(ex)
                        .isExactlyInstanceOf(ExitException.class)
                        .hasFieldOrPropertyWithValue("status", 0);
            }
        }

        @Test
        void withUnknownArgument_ShouldPrintErrorHelpAndExitWithCode2() {

            try {
                SystemLogHandler.startCapture();
                Convert.main(new String[]{"-wyz"});

            } catch (Exception ex) {
                final String capturedlog = SystemLogHandler.stopCapture();
                assertThat(capturedlog)
                        .contains(
                                "Unrecognized option: -wyz",
                                "jodconverter-cli [options] infile outfile [infile outfile ...]");
                assertThat(ex)
                        .isExactlyInstanceOf(ExitException.class)
                        .hasFieldOrPropertyWithValue("status", 2);
            }
        }

        @Test
        void withMissingsFilenames_ShouldPrintErrorHelpAndExitWithCode255() {

            try {
                SystemLogHandler.startCapture();
                Convert.main(new String[]{""});

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
        void withWrongFilenamesLength_ShouldPrintErrorHelpAndExitWithCode255() {

            try {
                SystemLogHandler.startCapture();
                Convert.main(new String[]{"input1.txt", "output1.pdf", "input2.txt"});

            } catch (Exception ex) {
                final String capturedlog = SystemLogHandler.stopCapture();
                assertThat(capturedlog)
                        .contains("jodconverter-cli [options] infile outfile [infile outfile ...]");
                assertThat(ex)
                        .isExactlyInstanceOf(ExitException.class)
                        .hasFieldOrPropertyWithValue("status", 255);
            }
        }
    }

    @Nested
    class CreateOfficeManager {

        @Test
        void withDefaultProperties_ShouldCreateManagerWithDefaultProperties() throws Exception {

            final CommandLine commandLine =
                    new DefaultParser()
                            .parse(
                                    (Options) ReflectionTestUtils.getField(Convert.class, "OPTIONS"),
                                    new String[]{"output1.pdf", "input2.txt"});

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
                    .hasFieldOrPropertyWithValue("taskQueueTimeout", DEFAULT_TASK_QUEUE_TIMEOUT);
            assertThat(officeManager)
                    .extracting("entries")
                    .asList()
                    .hasSize(1)
                    .element(0)
                    .satisfies(
                            o ->
                                    assertThat(o)
                                            .extracting(
                                                    "taskExecutionTimeout",
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
                                                    "officeProcessManager.startFailFast",
                                                    "officeProcessManager.keepAliveOnShutdown",
                                                    "officeProcessManager.connection.officeUrl.connectString")
                                            .containsExactly(
                                                    DEFAULT_TASK_EXECUTION_TIMEOUT,
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
                                                    true,
                                                    DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
                                                    "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
        }

        @Test
        @SuppressWarnings("ResultOfMethodCallIgnored")
        void withCustomValues_ShouldInitializedManagerWithCustomValues() throws Exception {

            final File ooHome = new File(testFolder, "oohomecustom");
            final File program = new File(ooHome, "program");
            program.mkdirs();
            new File(program, "soffice.bin").createNewFile(); // EXECUTABLE_DEFAULT
            new File(program, "soffice").createNewFile(); // EXECUTABLE_MAC
            new File(program, "soffice.exe").createNewFile(); // EXECUTABLE_WINDOWS
            final File macos = new File(ooHome, "MacOS");
            macos.mkdirs();
            new File(macos, "soffice").createNewFile(); // EXECUTABLE_MAC_41
            program.mkdirs();

            final CommandLine commandLine =
                    new DefaultParser()
                            .parse(
                                    (Options) ReflectionTestUtils.getField(Convert.class, "OPTIONS"),
                                    new String[]{
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
                                            "output1.pdf"
                                    });

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
                    .hasFieldOrPropertyWithValue("taskQueueTimeout", DEFAULT_TASK_QUEUE_TIMEOUT);
            assertThat(officeManager)
                    .extracting("entries")
                    .asList()
                    .hasSize(1)
                    .element(0)
                    .satisfies(
                            o ->
                                    assertThat(o)
                                            .extracting(
                                                    "taskExecutionTimeout",
                                                    "maxTasksPerProcess",
                                                    "officeProcessManager.officeUrl.connectString",
                                                    "officeProcessManager.officeHome",
                                                    "officeProcessManager.hostName",
                                                    "officeProcessManager.processManager.class.name",
                                                    "officeProcessManager.runAsArgs",
                                                    "officeProcessManager.templateProfileDir",
                                                    "officeProcessManager.processTimeout",
                                                    "officeProcessManager.processRetryInterval",
                                                    "officeProcessManager.afterStartProcessDelay",
                                                    "officeProcessManager.existingProcessAction",
                                                    "officeProcessManager.startFailFast",
                                                    "officeProcessManager.keepAliveOnShutdown",
                                                    "officeProcessManager.connection.officeUrl.connectString")
                                            .containsExactly(
                                                    30_000L,
                                                    DEFAULT_MAX_TASKS_PER_PROCESS,
                                                    "socket,host=127.0.0.1,port=2003,tcpNoDelay=1",
                                                    ooHome,
                                                    "localhost",
                                                    LocalOfficeUtils.findBestProcessManager().getClass().getName(),
                                                    Collections.EMPTY_LIST,
                                                    new File("src/test/resources/templateProfileDir"),
                                                    DEFAULT_PROCESS_TIMEOUT,
                                                    DEFAULT_PROCESS_RETRY_INTERVAL,
                                                    DEFAULT_AFTER_START_PROCESS_DELAY,
                                                    ExistingProcessAction.KILL,
                                                    true,
                                                    true,
                                                    "socket,host=127.0.0.1,port=2003,tcpNoDelay=1"));
        }

        @Test
        void withExistingProcessActionFail_ShouldInitializedManagerWithCustomValues() throws Exception {

            final CommandLine commandLine =
                    new DefaultParser()
                            .parse(
                                    (Options) ReflectionTestUtils.getField(Convert.class, "OPTIONS"),
                                    new String[]{
                                            "-x", ExistingProcessAction.FAIL.toString(), "input1.txt", "output1.pdf"
                                    });

            final OfficeManager officeManager =
                    ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);

            assertThat(officeManager)
                    .extracting("entries")
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

            final CommandLine commandLine =
                    new DefaultParser()
                            .parse(
                                    (Options) ReflectionTestUtils.getField(Convert.class, "OPTIONS"),
                                    new String[]{
                                            "-x", ExistingProcessAction.CONNECT.toString(), "input1.txt", "output1.pdf"
                                    });

            final OfficeManager officeManager =
                    ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);

            assertThat(officeManager)
                    .extracting("entries")
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

            final CommandLine commandLine =
                    new DefaultParser()
                            .parse(
                                    (Options) ReflectionTestUtils.getField(Convert.class, "OPTIONS"),
                                    new String[]{
                                            "-x",
                                            ExistingProcessAction.CONNECT_OR_KILL.toString(),
                                            "input1.txt",
                                            "output1.pdf"
                                    });

            final OfficeManager officeManager =
                    ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);

            assertThat(officeManager)
                    .extracting("entries")
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

            final CommandLine commandLine =
                    new DefaultParser()
                            .parse(
                                    (Options) ReflectionTestUtils.getField(Convert.class, "OPTIONS"),
                                    new String[]{"-lPassword=myPassword", "output1.pdf", "input2.txt"});

            final OfficeManager officeManager =
                    ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);
            Assertions.assertNotNull(officeManager);
            final CliConverter cliConverter =
                    ReflectionTestUtils.invokeMethod(
                            Convert.class, "createCliConverter", commandLine, null, officeManager, null);
            Assertions.assertNotNull(cliConverter);
            final LocalConverter localConverter =
                    (LocalConverter) ReflectionTestUtils.getField(cliConverter, "converter");
            Assertions.assertNotNull(localConverter);

            final Map<String, Object> expectedLoadProperties =
                    new HashMap<>(LocalConverter.DEFAULT_LOAD_PROPERTIES);
            expectedLoadProperties.put("Password", "myPassword");
            assertThat(localConverter).extracting("loadProperties").isEqualTo(expectedLoadProperties);
        }

        @Test
        void withFilterDataProperties_ShouldCreateConverterWithExpectedProperties() throws Exception {

            final CommandLine commandLine =
                    new DefaultParser()
                            .parse(
                                    (Options) ReflectionTestUtils.getField(Convert.class, "OPTIONS"),
                                    new String[]{"-sFDPageRange=2-2", "output1.pdf", "input2.txt"});

            final OfficeManager officeManager =
                    ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);
            Assertions.assertNotNull(officeManager);
            final CliConverter cliConverter =
                    ReflectionTestUtils.invokeMethod(
                            Convert.class, "createCliConverter", commandLine, null, officeManager, null);
            Assertions.assertNotNull(cliConverter);
            final LocalConverter localConverter =
                    (LocalConverter) ReflectionTestUtils.getField(cliConverter, "converter");
            Assertions.assertNotNull(localConverter);

            final Map<String, Object> expectedFilterData = new HashMap<>();
            expectedFilterData.put("PageRange", "2-2");
            final Map<String, Object> expectedStoreProperties = new HashMap<>();
            expectedStoreProperties.put("FilterData", expectedFilterData);
            assertThat(localConverter).extracting("storeProperties").isEqualTo(expectedStoreProperties);
        }

        @Test
        void withStoreProperties_ShouldCreateConverterWithExpectedProperties() throws Exception {

            final CommandLine commandLine =
                    new DefaultParser()
                            .parse(
                                    (Options) ReflectionTestUtils.getField(Convert.class, "OPTIONS"),
                                    new String[]{"-sOverwrite=true", "output1.pdf", "input2.txt"});

            final OfficeManager officeManager =
                    ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);
            Assertions.assertNotNull(officeManager);
            final CliConverter cliConverter =
                    ReflectionTestUtils.invokeMethod(
                            Convert.class, "createCliConverter", commandLine, null, officeManager, null);
            Assertions.assertNotNull(cliConverter);
            final LocalConverter localConverter =
                    (LocalConverter) ReflectionTestUtils.getField(cliConverter, "converter");
            Assertions.assertNotNull(localConverter);

            final Map<String, Object> expectedStoreProperties = new HashMap<>();
            expectedStoreProperties.put("Overwrite", true);
            assertThat(localConverter).extracting("storeProperties").isEqualTo(expectedStoreProperties);
        }

        @Test
        void withStoreAndFilterDataProperties_ShouldCreateConverterWithExpectedProperties()
                throws Exception {

            final CommandLine commandLine =
                    new DefaultParser()
                            .parse(
                                    (Options) ReflectionTestUtils.getField(Convert.class, "OPTIONS"),
                                    new String[]{
                                            "-sOverwrite=true",
                                            "-sReadOnly=false",
                                            "-sFDPageRange=2-4",
                                            "-sFDIntProp=5",
                                            "-sFD=NotFilterData",
                                            "output1.pdf",
                                            "input2.txt"
                                    });

            final OfficeManager officeManager =
                    ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);
            Assertions.assertNotNull(officeManager);
            final CliConverter cliConverter =
                    ReflectionTestUtils.invokeMethod(
                            Convert.class, "createCliConverter", commandLine, null, officeManager, null);
            Assertions.assertNotNull(cliConverter);
            final LocalConverter localConverter =
                    (LocalConverter) ReflectionTestUtils.getField(cliConverter, "converter");
            Assertions.assertNotNull(localConverter);

            final Map<String, Object> expectedFilterData = new HashMap<>();
            expectedFilterData.put("PageRange", "2-4");
            expectedFilterData.put("IntProp", 5);
            final Map<String, Object> expectedStoreProperties = new HashMap<>();
            expectedStoreProperties.put("Overwrite", true);
            expectedStoreProperties.put("ReadOnly", false);
            expectedStoreProperties.put("FD", "NotFilterData");
            expectedStoreProperties.put("FilterData", expectedFilterData);
            assertThat(localConverter).extracting("storeProperties").isEqualTo(expectedStoreProperties);
        }

        @Test
        void withBadLoadProperties_ShouldIgnoreBadLoadProperties() throws Exception {

            final CommandLine commandLine =
                    new DefaultParser()
                            .parse(
                                    (Options) ReflectionTestUtils.getField(Convert.class, "OPTIONS"),
                                    new String[]{"-lPassword", "output1.pdf", "input2.txt"});

            final OfficeManager officeManager =
                    ReflectionTestUtils.invokeMethod(Convert.class, "createOfficeManager", commandLine, null);
            Assertions.assertNotNull(officeManager);
            final CliConverter cliConverter =
                    ReflectionTestUtils.invokeMethod(
                            Convert.class, "createCliConverter", commandLine, null, officeManager, null);
            Assertions.assertNotNull(cliConverter);
            final LocalConverter localConverter =
                    (LocalConverter) ReflectionTestUtils.getField(cliConverter, "converter");
            Assertions.assertNotNull(localConverter);

            assertThat(localConverter)
                    .extracting("loadProperties")
                    .isEqualTo(LocalConverter.DEFAULT_LOAD_PROPERTIES);
        }
    }
}
