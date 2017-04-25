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
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.beanutils.BeanDeclaration;
import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.beanutils.XMLBeanDeclaration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;

import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.document.JsonDocumentFormatRegistry;
import org.artofsolving.jodconverter.filter.FilterChain;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerBuilder;

/** Command line interface executable. */
public final class Convert {

  public static final int STATUS_OK = 0;
  public static final int STATUS_MISSING_INPUT_FILE = 1;
  public static final int STATUS_INVALID_ARGUMENTS = 255;

  private static final Option OPTION_FILTER_CHAIN =
      Option.builder("c")
          .longOpt("filter-chain")
          .argName("file")
          .hasArg()
          .desc("Filter chain configuration file (optional)")
          .build();
  private static final Option OPTION_OUTPUT_DIRECTORY =
      Option.builder("d")
          .longOpt("output-directory")
          .argName("dir")
          .hasArg()
          .desc("output directory (optional; defaults to input directory)")
          .build();
  private static final Option OPTION_OUTPUT_FORMAT =
      Option.builder("f")
          .longOpt("output-format")
          .hasArg()
          .desc("output format (e.g. pdf)")
          .build();
  private static final Option OPTION_HELP =
      Option.builder("h").longOpt("help").desc("print help message").build();
  private static final Option OPTION_OFFICE_HOME =
      Option.builder("i")
          .longOpt("office-home")
          .argName("dir")
          .hasArg()
          .desc("office home directory (optional; defaults to auto-detect)")
          .build();
  private static final Option OPTION_KILL_EXISTING_PROCESS =
      Option.builder("k")
          .longOpt("kill-process")
          .desc("Kill existing office process (optional)")
          .build();
  private static final Option OPTION_PROCESS_MANAGER =
      Option.builder("m")
          .longOpt("process-manager")
          .argName("classname")
          .hasArg()
          .desc("class name of the process manager to use (optional; defaults to auto-detect)")
          .build();
  private static final Option OPTION_OVERWRITE =
      Option.builder("o")
          .longOpt("overwrite")
          .desc("overwrite existing output file (optional; defaults to false)")
          .build();
  private static final Option OPTION_PORT =
      Option.builder("p")
          .longOpt("port")
          .hasArg()
          .desc("office socket port (optional; defaults to 2002)")
          .build();
  private static final Option OPTION_REGISTRY =
      Option.builder("r")
          .longOpt("registry")
          .argName("file")
          .hasArg()
          .desc("document formats registry configuration file (optional)")
          .build();
  private static final Option OPTION_TIMEOUT =
      Option.builder("t")
          .longOpt("timeout")
          .hasArg()
          .desc("maximum conversion time in seconds (optional; defaults to 120)")
          .build();
  private static final Option OPTION_USER_PROFILE =
      Option.builder("u")
          .longOpt("user-profile")
          .argName("dir")
          .hasArg()
          .desc("use settings from the given user installation dir (optional)")
          .build();
  private static final Option OPTION_VERSION =
      Option.builder("v").longOpt("version").desc("print version").build();

  private static final Options OPTIONS = initOptions();

  private static void checkPrintInfoAndExit(final CommandLine commandLine) {

    if (commandLine.hasOption(OPTION_HELP.getOpt())) {
      printHelp();
      System.exit(0);
    }

    if (commandLine.hasOption(OPTION_VERSION.getOpt())) {
      final Package p = Convert.class.getPackage();
      printInfo("jodconverter-cli version " + p.getImplementationVersion());
      System.exit(0);
    }
  }

  private static DefaultOfficeManagerBuilder createDefaultOfficeManagerBuilder(
      final CommandLine commandLine) {

    final DefaultOfficeManagerBuilder configuration = new DefaultOfficeManagerBuilder();
    //configuration.setWorkingDir(new File(Paths.get(".").toAbsolutePath().normalize().toString()));

    if (commandLine.hasOption(OPTION_OFFICE_HOME.getOpt())) {
      configuration.setOfficeHome(commandLine.getOptionValue(OPTION_OFFICE_HOME.getOpt()));
    }

    configuration.setKillExistingProcess(
        commandLine.hasOption(OPTION_KILL_EXISTING_PROCESS.getOpt()));

    if (commandLine.hasOption(OPTION_PROCESS_MANAGER.getOpt())) {
      configuration.setProcessManager(commandLine.getOptionValue(OPTION_PROCESS_MANAGER.getOpt()));
    }

    if (commandLine.hasOption(OPTION_PORT.getOpt())) {
      configuration.setPortNumber(
          Integer.parseInt(commandLine.getOptionValue(OPTION_PORT.getOpt())));
    }

    if (commandLine.hasOption(OPTION_TIMEOUT.getOpt())) {
      configuration.setTaskExecutionTimeout(
          Long.parseLong(commandLine.getOptionValue(OPTION_TIMEOUT.getOpt())) * 1000L);
    }

    if (commandLine.hasOption(OPTION_USER_PROFILE.getOpt())) {
      configuration.setTemplateProfileDir(
          new File(commandLine.getOptionValue(OPTION_USER_PROFILE.getOpt())));
    }

    return configuration;
  }

  private static FilterChain getFilterChainOption(final CommandLine commandLine)
      throws ConfigurationException {

    if (commandLine.hasOption(OPTION_FILTER_CHAIN.getOpt())) {

      final Parameters params = new Parameters();
      final FileBasedConfigurationBuilder<XMLConfiguration> builder =
          new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class)
              .configure(
                  params
                      .xml()
                      .setFileName(commandLine.getOptionValue(OPTION_FILTER_CHAIN.getOpt())));
      final XMLConfiguration config = builder.getConfiguration();

      // Create the filter chain from the XML configuration
      final BeanDeclaration decl = new XMLBeanDeclaration(config, "filterChain");
      final BeanHelper helper = new BeanHelper(new FilterChainBeanFactory());
      return (FilterChain) helper.createBean(decl);
    }

    return null;
  }

  private static DocumentFormatRegistry getRegistryOption(final CommandLine commandLine)
      throws IOException {

    if (commandLine.hasOption(OPTION_REGISTRY.getOpt())) {
      return JsonDocumentFormatRegistry.create(
          FileUtils.readFileToString(
              new File(commandLine.getOptionValue(OPTION_REGISTRY.getOpt())), "UTF-8"));
    }

    return null;
  }

  private static String getStringOption(final CommandLine commandLine, final String option) {

    if (commandLine.hasOption(option)) {
      return commandLine.getOptionValue(option);
    }

    // Default
    return null;
  }

  private static Options initOptions() {

    final Options options = new Options();
    options.addOption(OPTION_HELP);
    options.addOption(OPTION_VERSION);
    options.addOption(OPTION_FILTER_CHAIN);
    options.addOption(OPTION_OUTPUT_DIRECTORY);
    options.addOption(OPTION_OUTPUT_FORMAT);
    options.addOption(OPTION_OFFICE_HOME);
    options.addOption(OPTION_KILL_EXISTING_PROCESS);
    options.addOption(OPTION_PROCESS_MANAGER);
    options.addOption(OPTION_OVERWRITE);
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
  public static void main(final String[] arguments) {

    try {
      final CommandLine commandLine = new DefaultParser().parse(OPTIONS, arguments);

      // Check if the command line contains arguments that is suppose
      // to print some info and then exit.
      checkPrintInfoAndExit(commandLine);

      // Get conversion arguments
      final String outputFormat = getStringOption(commandLine, OPTION_OUTPUT_FORMAT.getOpt());
      final String outputDirPath = getStringOption(commandLine, OPTION_OUTPUT_DIRECTORY.getOpt());
      final DocumentFormatRegistry registry = getRegistryOption(commandLine);
      final FilterChain filterChain = getFilterChainOption(commandLine);
      final boolean overwrite = commandLine.hasOption(OPTION_OVERWRITE.getOpt());
      final String[] filenames = commandLine.getArgs();

      // Validate arguments length
      if ((outputFormat == null && filenames.length % 2 != 0) || filenames.length == 0) {
        printHelp();
        System.exit(STATUS_INVALID_ARGUMENTS);
      }

      // Create and configure a DefaultOfficeManagerBuilder from the command line
      final DefaultOfficeManagerBuilder configuration =
          createDefaultOfficeManagerBuilder(commandLine);

      // Build a client converter and start the conversion
      try (final CliConverter converter = new CliConverter(configuration, registry)) {

        if (outputFormat == null) {

          // Build 2 arrays; one containing the input files and the other
          // containing the output files.
          final String[] inputFilenames = new String[filenames.length / 2];
          final String[] outputFilenames = new String[inputFilenames.length];
          for (int i = 0, j = 0; i < filenames.length; i += 2, j++) {
            inputFilenames[j] = filenames[i];
            outputFilenames[j] = filenames[i + 1];
          }
          converter.convert(inputFilenames, outputFilenames, outputDirPath, overwrite, filterChain);

        } else {

          converter.convert(filenames, outputFormat, outputDirPath, overwrite, filterChain);
        }
      }

    } catch (ParseException e) {
      printErr("jodconverter-cli: " + e.getMessage());
      printHelp();
      System.exit(2);
    } catch (Exception e) { // NOSONAR
      printErr("jodconverter-cli: " + e.getMessage());
      e.printStackTrace(System.err); // NOSONAR
      System.exit(2);
    }
  }

  private static void printHelp() {

    new HelpFormatter()
        .printHelp(
            "jodconverter-cli [options] infile outfile [infile outfile ...]\n"
                + "   or: jodconverter-cli [options] -f output-format infile [infile ...]",
            OPTIONS);
  }

  private static void printInfo(final String info) {

    final PrintWriter pw = new PrintWriter(System.out); // NOSONAR
    pw.println(info);
    pw.flush();
  }

  private static void printErr(final String err) {

    final PrintWriter pw = new PrintWriter(System.err); // NOSONAR
    pw.println(err);
    pw.flush();
  }

  // Private ctor.
  private Convert() { // NOSONAR
    throw new AssertionError("utility class must not be instantiated");
  }
}
