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
import java.io.FileFilter;
import java.io.IOException;

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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.document.JsonDocumentFormatRegistry;
import org.artofsolving.jodconverter.filter.DefaultFilterChain;
import org.artofsolving.jodconverter.filter.FilterChain;
import org.artofsolving.jodconverter.filter.RefreshFilter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerBuilder;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;

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
      System.out.println("jodconverter-cli version " + p.getImplementationVersion()); // NOSONAR
      System.exit(0);
    }
  }

  private static void convertSources(
      final OfficeDocumentConverter converter,
      final String[] sources,
      final File outputDir,
      final String outputFormat,
      final FilterChain filterChain)
      throws OfficeException {

    if (outputFormat == null) {

      // For all the input/output pairs...
      for (int i = 0; i < sources.length; i += 2) {

        final File sourceFile = new File(sources[i]);

        String targetFullPath = FilenameUtils.getFullPath(sources[i + 1]);
        String targetName = FilenameUtils.getName(sources[i + 1]);
        String targetDirectory =
            StringUtils.isBlank(targetFullPath)
                ? FilenameUtils.getFullPath(sources[i])
                : targetFullPath;

        converter.convert(
            filterChain,
            sourceFile,
            new File(outputDir == null ? new File(targetDirectory) : outputDir, targetName));
      }

    } else {

      // For all the input arguments...
      for (final String source : sources) {

        // Create a file instance with the argument and also get the parent directory
        final File sourceFile = new File(source);
        final File sourceFileParent = new File(FilenameUtils.getFullPath(source));

        // If the argument is a file, just convert it.
        if (sourceFile.isFile()) {
          converter.convert(
              filterChain,
              new File(source),
              new File(
                  outputDir == null ? sourceFileParent : outputDir,
                  FilenameUtils.getBaseName(source) + "." + outputFormat));
        } else {

          // If the argument is not a file, check if it has a wildcard
          // to match multiple files
          if (sourceFileParent.isDirectory()) {
            final String wildcard = FilenameUtils.getBaseName(source);
            final File[] files =
                sourceFileParent.listFiles((FileFilter) new WildcardFileFilter(wildcard));
            for (final File file : files) {
              converter.convert(
                  filterChain,
                  file,
                  new File(
                      outputDir == null ? sourceFileParent : outputDir,
                      FilenameUtils.getBaseName(file.getName()) + "." + outputFormat));
            }
          }
        }
      }
    }
  }

  private static DefaultOfficeManagerBuilder createDefaultOfficeManagerBuilder(
      final CommandLine commandLine) {

    final DefaultOfficeManagerBuilder configuration = new DefaultOfficeManagerBuilder();

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

    // Default
    return new DefaultFilterChain(RefreshFilter.INSTANCE);
  }

  private static DocumentFormatRegistry getRegistryOption(final CommandLine commandLine)
      throws IOException {

    if (commandLine.hasOption(OPTION_REGISTRY.getOpt())) {
      return JsonDocumentFormatRegistry.create(
          FileUtils.readFileToString(
              new File(commandLine.getOptionValue(OPTION_REGISTRY.getOpt())), "UTF-8"));
    }

    // Default
    return DefaultDocumentFormatRegistry.create();
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
      final String[] sources = commandLine.getArgs();
      if ((outputFormat == null && sources.length % 2 != 0) || sources.length == 0) {
        printHelp();
        System.exit(STATUS_INVALID_ARGUMENTS);
      }

      // Create and configure a DefaultOfficeManagerBuilder from the command line
      final DefaultOfficeManagerBuilder configuration =
          createDefaultOfficeManagerBuilder(commandLine);

      // Build an OfficeManager and convert sources arguments
      final OfficeManager officeManager = configuration.build();
      officeManager.start();
      try {
        final OfficeDocumentConverter converter =
            new OfficeDocumentConverter(officeManager, registry);

        // Ensure output directory exists
        File outputDir = null;
        if (outputDirPath != null) {
          outputDir = new File(outputDirPath);
          FileUtils.forceMkdir(outputDir);
        }

        // Convert all the sources arguments
        convertSources(converter, sources, outputDir, outputFormat, filterChain);

      } finally {
        officeManager.stop();
      }

    } catch (ParseException e) {
      System.err.println("jodconverter-cli: " + e.getMessage()); // NOSONAR
      printHelp();
      System.exit(2);
    } catch (Exception e) { // NOSONAR
      System.err.println("jodconverter-cli: " + e.getMessage()); // NOSONAR
      e.printStackTrace(); // NOSONAR
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

  // Private ctor.
  private Convert() { // NOSONAR
    throw new AssertionError("utility class must not be instantiated");
  }
}
