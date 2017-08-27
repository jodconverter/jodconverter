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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import org.jodconverter.LocalConverter;
import org.jodconverter.OnlineConverter;
import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.document.JsonDocumentFormatRegistry;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.LocalOfficeManager;
import org.jodconverter.office.LocalOfficeUtils;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.office.OnlineOfficeManager;

/** Command line interface executable. */
public final class Convert {

  public static final int STATUS_OK = 0;
  public static final int STATUS_MISSING_INPUT_FILE = 1;
  public static final int STATUS_INVALID_ARGUMENTS = 255;

  private static final Option OPT_APPLICATION_CONTEXT =
      Option.builder("a")
          .longOpt("application-context")
          .argName("file")
          .hasArg()
          .desc("Application context file (optional)")
          .build();
  private static final Option OPT_CONNECTION_URL =
      Option.builder("c")
          .longOpt("connection-url")
          .argName("url")
          .hasArg()
          .desc("remote LibreOffice Online server URL for conversion")
          .build();
  private static final Option OPT_OUTPUT_DIRECTORY =
      Option.builder("d")
          .longOpt("output-directory")
          .argName("dir")
          .hasArg()
          .desc("output directory (optional; defaults to input directory)")
          .build();
  private static final Option OPT_OUTPUT_FORMAT =
      Option.builder("f")
          .longOpt("output-format")
          .hasArg()
          .desc("output format (e.g. pdf)")
          .build();
  private static final Option OPT_HELP =
      Option.builder("h").longOpt("help").desc("print help message").build();
  private static final Option OPT_OFFICE_HOME =
      Option.builder("i")
          .longOpt("office-home")
          .argName("dir")
          .hasArg()
          .desc("office home directory (optional; defaults to auto-detect)")
          .build();
  private static final Option OPT_KILL_EXISTING_PROCESS =
      Option.builder("k")
          .longOpt("kill-process")
          .desc("Kill existing office process (optional)")
          .build();
  private static final Option OPT_PROCESS_MANAGER =
      Option.builder("m")
          .longOpt("process-manager")
          .argName("classname")
          .hasArg()
          .desc("class name of the process manager to use (optional; defaults to auto-detect)")
          .build();
  private static final Option OPT_OVERWRITE =
      Option.builder("o")
          .longOpt("overwrite")
          .desc("overwrite existing output file (optional; defaults to false)")
          .build();
  private static final Option OPT_PORT =
      Option.builder("p")
          .longOpt("port")
          .hasArg()
          .desc("office socket port (optional; defaults to 2002)")
          .build();
  private static final Option OPT_REGISTRY =
      Option.builder("r")
          .longOpt("registry")
          .argName("file")
          .hasArg()
          .desc("document formats registry configuration file (optional)")
          .build();
  private static final Option OPT_TIMEOUT =
      Option.builder("t")
          .longOpt("timeout")
          .hasArg()
          .desc("maximum conversion time in seconds (optional; defaults to 120)")
          .build();
  private static final Option OPT_USER_PROFILE =
      Option.builder("u")
          .longOpt("user-profile")
          .argName("dir")
          .hasArg()
          .desc("use settings from the given user installation dir (optional)")
          .build();
  private static final Option OPT_VERSION =
      Option.builder("v").longOpt("version").desc("print version").build();

  private static final Options OPTIONS = initOptions();

  private static void checkPrintInfoAndExit(final CommandLine commandLine) {

    if (commandLine.hasOption(OPT_HELP.getOpt())) {
      printHelp();
      System.exit(0);
    }

    if (commandLine.hasOption(OPT_VERSION.getOpt())) {
      final Package pack = Convert.class.getPackage();
      printInfo("jodconverter-cli version %s", pack.getImplementationVersion());
      System.exit(0);
    }
  }

  private static OfficeManager createOfficeManager(final CommandLine commandLine) {

    // If the URL is present, we will use the online office manager and thus,
    // an office installation won't be required locally.
    if (commandLine.hasOption(OPT_CONNECTION_URL.getOpt())) {
      final String connectionUrl = getStringOption(commandLine, OPT_CONNECTION_URL.getOpt());
      return OnlineOfficeManager.builder().urlConnection(connectionUrl).build();
    }

    // Not online conversion...

    final LocalOfficeManager.Builder builder = LocalOfficeManager.builder();
    //configuration.setWorkingDir(new File(Paths.get(".").toAbsolutePath().normalize().toString()));

    if (commandLine.hasOption(OPT_OFFICE_HOME.getOpt())) {
      builder.officeHome(commandLine.getOptionValue(OPT_OFFICE_HOME.getOpt()));
    }

    builder.killExistingProcess(commandLine.hasOption(OPT_KILL_EXISTING_PROCESS.getOpt()));

    if (commandLine.hasOption(OPT_PROCESS_MANAGER.getOpt())) {
      builder.processManager(commandLine.getOptionValue(OPT_PROCESS_MANAGER.getOpt()));
    }

    if (commandLine.hasOption(OPT_PORT.getOpt())) {
      builder.portNumbers(Integer.parseInt(commandLine.getOptionValue(OPT_PORT.getOpt())));
    }

    if (commandLine.hasOption(OPT_TIMEOUT.getOpt())) {
      builder.taskExecutionTimeout(
          Long.parseLong(commandLine.getOptionValue(OPT_TIMEOUT.getOpt())) * 1000L);
    }

    if (commandLine.hasOption(OPT_USER_PROFILE.getOpt())) {
      builder.templateProfileDir(new File(commandLine.getOptionValue(OPT_USER_PROFILE.getOpt())));
    }

    return builder.install().build();
  }

  private static AbstractApplicationContext getApplicationContextOption(
      final CommandLine commandLine) {

    if (commandLine.hasOption(OPT_APPLICATION_CONTEXT.getOpt())) {

      return new FileSystemXmlApplicationContext(
          commandLine.getOptionValue(OPT_APPLICATION_CONTEXT.getOpt()));
    }

    return null;
  }

  private static FilterChain getFilterChain(final ApplicationContext context) {

    if (context != null) {
      return (FilterChain) context.getBean("filterChain");
    }

    return null;
  }

  private static DocumentFormatRegistry getRegistryOption(final CommandLine commandLine)
      throws IOException {

    if (commandLine.hasOption(OPT_REGISTRY.getOpt())) {
      return JsonDocumentFormatRegistry.create(
          FileUtils.readFileToString(
              new File(commandLine.getOptionValue(OPT_REGISTRY.getOpt())), "UTF-8"));
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
    options.addOption(OPT_HELP);
    options.addOption(OPT_VERSION);
    options.addOption(OPT_APPLICATION_CONTEXT);
    options.addOption(OPT_OUTPUT_DIRECTORY);
    options.addOption(OPT_OUTPUT_FORMAT);
    options.addOption(OPT_OFFICE_HOME);
    options.addOption(OPT_KILL_EXISTING_PROCESS);
    options.addOption(OPT_PROCESS_MANAGER);
    options.addOption(OPT_OVERWRITE);
    options.addOption(OPT_PORT);
    options.addOption(OPT_REGISTRY);
    options.addOption(OPT_TIMEOUT);
    options.addOption(OPT_USER_PROFILE);
    options.addOption(OPT_CONNECTION_URL);

    return options;
  }

  /**
   * Main entry point of the program.
   *
   * @param arguments program arguments.
   */
  public static void main(final String[] arguments) {

    try {
      final CommandLine commandLine = new DefaultParser().parse(OPTIONS, arguments);

      // Check if the command line contains arguments that is suppose
      // to print some info and then exit.
      checkPrintInfoAndExit(commandLine);

      // Get conversion arguments
      final String outputFormat = getStringOption(commandLine, OPT_OUTPUT_FORMAT.getOpt());
      final String outputDirPath = getStringOption(commandLine, OPT_OUTPUT_DIRECTORY.getOpt());
      final DocumentFormatRegistry registry = getRegistryOption(commandLine);
      final boolean overwrite = commandLine.hasOption(OPT_OVERWRITE.getOpt());
      final String[] filenames = commandLine.getArgs();

      // Validate arguments length
      if (outputFormat == null && filenames.length % 2 != 0 || filenames.length == 0) {
        printHelp();
        System.exit(STATUS_INVALID_ARGUMENTS);
      }

      // Load the application context if provided
      final AbstractApplicationContext context = getApplicationContextOption(commandLine);

      // Create a default office manager from the command line
      final OfficeManager officeManager = createOfficeManager(commandLine);

      try {
        // Starts the manager
        printInfo("Starting office");
        officeManager.start();

        // Build a client converter and start the conversion
        final CliConverter converter =
            createCliConverter(commandLine, context, officeManager, registry);

        if (outputFormat == null) {

          // Build 2 arrays; one containing the input files and the other
          // containing the output files.
          final String[] inputFilenames = new String[filenames.length / 2];
          final String[] outputFilenames = new String[inputFilenames.length];
          for (int i = 0, j = 0; i < filenames.length; i += 2, j++) {
            inputFilenames[j] = filenames[i];
            outputFilenames[j] = filenames[i + 1];
          }
          converter.convert(inputFilenames, outputFilenames, outputDirPath, overwrite);

        } else {

          converter.convert(filenames, outputFormat, outputDirPath, overwrite);
        }
      } finally {
        printInfo("Stopping office");
        LocalOfficeUtils.stopQuietly(officeManager);

        // Close the application context
        IOUtils.closeQuietly(context);
      }

      System.exit(0);

    } catch (ParseException e) {
      printErr("jodconverter-cli: %s", e.getMessage());
      printHelp();
      System.exit(2);
    } catch (Exception e) { // NOSONAR
      printErr("jodconverter-cli: %s", e.getMessage());
      e.printStackTrace(System.err); // NOSONAR
      System.exit(2);
    }
  }

  private static CliConverter createCliConverter(
      final CommandLine commandLine,
      final AbstractApplicationContext context,
      final OfficeManager officeManager,
      final DocumentFormatRegistry registry) {

    if (commandLine.hasOption(OPT_CONNECTION_URL.getOpt())) {
      return new CliConverter(
          OnlineConverter.builder().officeManager(officeManager).formatRegistry(registry).build());
    }

    final FilterChain filterChain = getFilterChain(context);
    final LocalConverter.Builder builder = LocalConverter.builder();
    builder.officeManager(officeManager).formatRegistry(registry);
    if (filterChain != null) {
      builder.filterWith(filterChain);
    }
    return new CliConverter(builder.build());
  }

  private static void printHelp() {

    new HelpFormatter()
        .printHelp(
            "jodconverter-cli [options] infile outfile [infile outfile ...]\n"
                + "   or: jodconverter-cli [options] -f output-format infile [infile ...]",
            OPTIONS);
  }

  private static void printErr(final String message, final Object... values) {

    final PrintWriter writer = new PrintWriter(System.err); // NOSONAR
    writer.println(String.format(message, values));
    writer.flush();
  }

  private static void printInfo(final String message, final Object... values) {

    final PrintWriter writer = new PrintWriter(System.out); // NOSONAR
    writer.println(String.format(message, values));
    writer.flush();
  }
}
