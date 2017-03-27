/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.cli;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.document.JsonDocumentFormatRegistry;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerBuilder;
import org.artofsolving.jodconverter.office.OfficeManager;

/** Command line interface executable. */
public final class Convert {

  public static final int STATUS_OK = 0;
  public static final int STATUS_MISSING_INPUT_FILE = 1;
  public static final int STATUS_INVALID_ARGUMENTS = 255;

  private static final Option OPTION_OUTPUT_FORMAT =
      new Option("o", "output-format", true, "output format (e.g. pdf)");
  private static final Option OPTION_PORT =
      new Option("p", "port", true, "office socket port (optional; defaults to 2002)");
  private static final Option OPTION_REGISTRY =
      new Option("r", "registry", true, "document formats registry configuration file (optional)");
  private static final Option OPTION_TIMEOUT =
      new Option(
          "t", "timeout", true, "maximum conversion time in seconds (optional; defaults to 120)");
  private static final Option OPTION_USER_PROFILE =
      new Option(
          "u",
          "user-profile",
          true,
          "use settings from the given user installation dir (optional)");

  private static final Options OPTIONS = initOptions();
  private static final int DEFAULT_OFFICE_PORT = 2002;

  private static Options initOptions() {

    final Options options = new Options();
    options.addOption(OPTION_OUTPUT_FORMAT);
    options.addOption(OPTION_PORT);
    options.addOption(OPTION_REGISTRY);
    options.addOption(OPTION_TIMEOUT);
    options.addOption(OPTION_USER_PROFILE);
    return options;
  }

  /**
   * Main entry point of the program.
   *
   * @param arguments program arguments.
   * @throws Exception if an error occurs.
   */
  public static void main(final String[] arguments) throws Exception { // NOSONAR

    final CommandLineParser commandLineParser = new DefaultParser();
    final CommandLine commandLine = commandLineParser.parse(OPTIONS, arguments);

    String outputFormat = null;
    if (commandLine.hasOption(OPTION_OUTPUT_FORMAT.getOpt())) {
      outputFormat = commandLine.getOptionValue(OPTION_OUTPUT_FORMAT.getOpt());
    }

    int port = DEFAULT_OFFICE_PORT;
    if (commandLine.hasOption(OPTION_PORT.getOpt())) {
      port = Integer.parseInt(commandLine.getOptionValue(OPTION_PORT.getOpt()));
    }

    final String[] fileNames = commandLine.getArgs();
    if ((outputFormat == null && fileNames.length != 2) || fileNames.length < 1) {
      new HelpFormatter()
          .printHelp(
              "java -jar jodconverter-core.jar [options] input-file output-file\n"
                  + "or [options] -o output-format input-file [input-file...]",
              OPTIONS);
      System.exit(STATUS_INVALID_ARGUMENTS);
    }

    DocumentFormatRegistry registry;
    if (commandLine.hasOption(OPTION_REGISTRY.getOpt())) {
      registry =
          JsonDocumentFormatRegistry.create(
              FileUtils.readFileToString(
                  new File(commandLine.getOptionValue(OPTION_REGISTRY.getOpt())), "UTF-8"));
    } else {
      registry = DefaultDocumentFormatRegistry.create();
    }

    final DefaultOfficeManagerBuilder configuration = new DefaultOfficeManagerBuilder();
    configuration.setPortNumber(port);
    if (commandLine.hasOption(OPTION_TIMEOUT.getOpt())) {
      configuration.setTaskExecutionTimeout(
          Long.parseLong(commandLine.getOptionValue(OPTION_TIMEOUT.getOpt())) * 1000L);
    }
    if (commandLine.hasOption(OPTION_USER_PROFILE.getOpt())) {
      configuration.setTemplateProfileDir(
          new File(commandLine.getOptionValue(OPTION_USER_PROFILE.getOpt())));
    }

    final OfficeManager officeManager = configuration.build();
    officeManager.start();
    try {
      final OfficeDocumentConverter converter =
          new OfficeDocumentConverter(officeManager, registry);
      if (outputFormat == null) {
        converter.convert(new File(fileNames[0]), new File(fileNames[1]));
      } else {
        for (int i = 0; i < fileNames.length; i++) {
          converter.convert(
              new File(fileNames[i]),
              new File(
                  FilenameUtils.getFullPath(fileNames[i])
                      + FilenameUtils.getBaseName(fileNames[i])
                      + "."
                      + outputFormat));
        }
      }
    } finally {
      officeManager.stop();
    }
  }

  // Private ctor.
  private Convert() { // NOSONAR
    throw new AssertionError("utility class must not be instantiated");
  }
}
