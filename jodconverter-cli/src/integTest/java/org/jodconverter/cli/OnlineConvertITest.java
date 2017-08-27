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

package org.jodconverter.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.jodconverter.cli.util.ExitException;
import org.jodconverter.cli.util.NoExitResource;
import org.jodconverter.cli.util.ResetExitExceptionResource;
import org.jodconverter.cli.util.SystemLogHandler;

/**
 * This class tests the {@link Convert} class, which contains the main function of the cli module.
 */
public class OnlineConvertITest {

  private static final String SOURCE_FILE_DOC = "src/integTest/resources/documents/test1.doc";
  private static final String SOURCE_FILE_TXT = "src/integTest/resources/documents/test1.txt";

  @ClassRule public static NoExitResource noExit = new NoExitResource();
  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().port(9980));

  @Rule public ResetExitExceptionResource resetExitEx = new ResetExitExceptionResource();

  @Test
  public void main_Convert_WithConnectionOptionAndOutputFormat_ShouldSucceed() throws Exception {

    final File inputFile = new File(SOURCE_FILE_DOC);
    FileUtils.copyFileToDirectory(inputFile, testFolder.getRoot());
    final File inputFileTmp =
        new File(testFolder.getRoot(), FilenameUtils.getName(SOURCE_FILE_DOC));
    final File outputFile =
        new File(testFolder.getRoot(), FilenameUtils.getBaseName(inputFile.getName()) + ".txt");
    assertThat(outputFile).doesNotExist();

    stubFor(
        post(urlEqualTo("/lool/convert-to/txt")).willReturn(aResponse().withBody("Test Document")));

    try {

      SystemLogHandler.startCapture();
      Convert.main(
          new String[] {
            inputFileTmp.getPath(), "-f", "txt", "-c", "http://localhost:9980/lool/convert-to/"
          });

      // Be sure the ExitException exception is thrown.
      fail();

    } catch (Exception ex) {
      try {
        SystemLogHandler.stopCapture();
        assertThat(ex)
            .isExactlyInstanceOf(ExitException.class)
            .hasFieldOrPropertyWithValue("status", 0);

        final String content = FileUtils.readFileToString(outputFile, Charset.forName("UTF-8"));
        System.out.println(content);
        assertThat(content).contains("Test Document");
      } finally {
        FileUtils.deleteQuietly(outputFile); // Prevent further test failure.
      }
    }
  }
}
