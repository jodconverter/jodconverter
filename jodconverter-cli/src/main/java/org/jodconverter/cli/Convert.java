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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.cli.*;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.cli.help.TextHelpAppendable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.document.JsonDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.pdf.PdfOptions;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.remote.RemoteConverter;
import org.jodconverter.remote.office.RemoteOfficeManager;
import org.jodconverter.remote.ssl.SslConfig;

/** Command line interface executable. */
public final class Convert {

  /** Status returned when the program runs without errors. */
  public static final int STATUS_OK = 0;

  // public static final int STATUS_MISSING_INPUT_FILE = 1;
  /** Status returned an error occurred while running the program. */
  public static final int STATUS_ERROR = 2;

  /** Status returned when the program arguments are invalid. */
  public static final int STATUS_INVALID_ARGUMENTS = 255;

  // Wide enough for most option descriptions to fit on one line (the default is 74).
  private static final int HELP_WIDTH = 120;

  private static final Option OPT_APPLICATION_CONTEXT =
      Option.builder("a")
          .longOpt("application-context")
          .argName("file")
          .hasArg()
          .desc("Application context file (optional)")
          .get();
  private static final Option OPT_CONNECTION_URL =
      Option.builder("c")
          .longOpt("connection-url")
          .argName("url")
          .hasArg()
          .desc("remote LibreOffice Online server URL for conversion")
          .get();
  private static final Option OPT_OUTPUT_DIRECTORY =
      Option.builder("d")
          .longOpt("output-directory")
          .argName("dir")
          .hasArg()
          .desc("output directory (optional; defaults to input directory)")
          .get();
  private static final Option OPT_OUTPUT_FORMAT =
      Option.builder("f").longOpt("output-format").hasArg().desc("output format (e.g. pdf)").get();
  private static final Option OPT_HELP =
      Option.builder("h").longOpt("help").desc("displays help at the command prompt").get();
  private static final Option OPT_OFFICE_HOME =
      Option.builder("i")
          .longOpt("office-home")
          .argName("dir")
          .hasArg()
          .desc("office home directory (optional; defaults to auto-detect)")
          .get();
  private static final Option OPT_KEEP_ALIVE =
      Option.builder("k")
          .longOpt("keep-alive")
          .desc("keep the office process alive on shutdown (optional; defaults to false)")
          .get();
  private static final Option OPT_LOAD_PROPERTIES =
      Option.builder("l")
          .longOpt("load-properties")
          .argName("name=value")
          .hasArg()
          .desc("load property; can be repeated (optional; eg. -lPassword=myPassword)")
          .get();
  private static final Option OPT_PROCESS_MANAGER =
      Option.builder("m")
          .longOpt("process-manager")
          .argName("classname")
          .hasArg()
          .desc("class name of the process manager to use (optional; defaults to auto-detect)")
          .get();
  private static final Option OPT_HOSTNAME =
      Option.builder("n")
          .longOpt("host-name")
          .hasArg()
          .desc("host name that will be used in the --accept argument when starting a process")
          .get();
  private static final Option OPT_OVERWRITE =
      Option.builder("o")
          .longOpt("overwrite")
          .desc("overwrite existing output file (optional; defaults to false)")
          .get();
  private static final Option OPT_PORT =
      Option.builder("p")
          .longOpt("port")
          .hasArg()
          .desc("office socket port (optional; defaults to 2002)")
          .get();
  private static final Option OPT_REGISTRY =
      Option.builder("r")
          .longOpt("registry")
          .argName("file")
          .hasArg()
          .desc("document formats registry configuration file (optional)")
          .get();
  private static final Option OPT_STORE_PROPERTIES =
      Option.builder("s")
          .longOpt("store-properties")
          .argName("name=value")
          .hasArg()
          .desc(
              "store property; can be repeated"
                  + " (optional; eg. -sOverwrite=true -sFDPageRange=1-2)")
          .get();
  private static final Option OPT_TIMEOUT =
      Option.builder("t")
          .longOpt("timeout")
          .hasArg()
          .desc("maximum conversion time in seconds (optional; defaults to 120)")
          .get();
  private static final Option OPT_USER_PROFILE =
      Option.builder("u")
          .longOpt("user-profile")
          .argName("dir")
          .hasArg()
          .desc("use settings from the given user installation dir (optional)")
          .get();
  private static final Option OPT_WORKING_DIR =
      Option.builder("w")
          .longOpt("working-dir")
          .argName("dir")
          .hasArg()
          .desc(
              "directory where temporary office profile directories will be created (optional; defaults to java.io.tmpdir)")
          .get();
  private static final Option OPT_VERSION =
      Option.builder("v").longOpt("version").desc("displays version information and exit").get();
  private static final Option OPT_EXISTING_PROCESS_ACTION =
      Option.builder("x")
          .longOpt("existing-process-action")
          .hasArg()
          .desc(
              "action taken when a process running with the same connection string"
                  + " (optional; defaults to kill);"
                  + " with fail: abort conversion;"
                  + " with kill: kill existing process;"
                  + " with connect: connect to existing process;"
                  + " with connect_or_kill: connect to existing process with kill fallback")
          .get();

  private static final Option OPT_PDF_PRESET =
      Option.builder()
          .longOpt("pdf-preset")
          .argName("name")
          .hasArg()
          .desc("PDF options to start from: archive, accessible or compact (optional)")
          .get();
  private static final Option OPT_PDF_OPTION =
      Option.builder()
          .longOpt("pdf-option")
          .argName("name=value")
          .hasArg()
          .desc(
              "option applied to the PDF outputs; can be repeated"
                  + " (optional; eg. --pdf-option version=pdf-a-2b --pdf-option pages.range=1-3)")
          .get();

  private static final Options OPTIONS = initOptions();

  // A load or store property attached to its short option, such as -sOverwrite=true.
  private static final Pattern ATTACHED_PROPERTY = Pattern.compile("^(-[ls])([^=]+=.*)$");

  // Returns true if the command line asked for some info, which is printed, and nothing else.
  private static boolean printInfoIfRequested(final CommandLine commandLine) {

    if (commandLine.hasOption(OPT_HELP.getOpt())) {
      printHelp();
      return true;
    }

    if (commandLine.hasOption(OPT_VERSION.getOpt())) {
      final var pack = Convert.class.getPackage();
      printInfo("jodconverter-cli version %s", pack.getImplementationVersion());
      return true;
    }

    return false;
  }

  private static OfficeManager createOfficeManager(
      final CommandLine commandLine, final AbstractApplicationContext context) {

    // If the URL is present, we will use the remote office manager and thus,
    // an office installation won't be required locally.
    if (commandLine.hasOption(OPT_CONNECTION_URL.getOpt())) {
      return createRemoteOfficeManager(commandLine, context);
    }

    // Not remote conversion...

    final var builder = LocalOfficeManager.builder();

    // Always fail fast!!
    builder.startFailFast(true);
    applyOption(OPT_OFFICE_HOME, commandLine, builder::officeHome);
    applyOption(OPT_WORKING_DIR, commandLine, builder::workingDir);
    applyOption(OPT_HOSTNAME, commandLine, builder::hostName);
    applyOption(OPT_PROCESS_MANAGER, commandLine, builder::processManager);
    applyOption(OPT_PORT, commandLine, opt -> builder.portNumbers(Integer.parseInt(opt)));
    applyOption(
        OPT_TIMEOUT, commandLine, opt -> builder.taskExecutionTimeout(Long.parseLong(opt) * 1000L));
    applyOption(OPT_USER_PROFILE, commandLine, builder::templateProfileDir);
    builder.keepAliveOnShutdown(commandLine.hasOption(OPT_KEEP_ALIVE.getOpt()));
    applyOption(
        OPT_EXISTING_PROCESS_ACTION,
        commandLine,
        opt ->
            builder.existingProcessAction(
                switch (opt.toLowerCase(Locale.ROOT).replace('-', '_')) {
                  case "fail" -> ExistingProcessAction.FAIL;
                  case "kill" -> ExistingProcessAction.KILL;
                  case "connect" -> ExistingProcessAction.CONNECT;
                  case "connect_or_kill" -> ExistingProcessAction.CONNECT_OR_KILL;
                  default -> LocalOfficeManager.DEFAULT_EXISTING_PROCESS_ACTION;
                }));

    return builder.install().build();
  }

  private static void applyOption(
      final Option option,
      final CommandLine commandLine,
      final Function<String, LocalOfficeManager.Builder> fn) {

    if (commandLine.hasOption(option.getOpt())) {
      fn.apply(commandLine.getOptionValue(option.getOpt()));
    }
  }

  private static OfficeManager createRemoteOfficeManager(
      final CommandLine commandLine, final AbstractApplicationContext context) {

    final var connectionUrl = getStringOption(commandLine, OPT_CONNECTION_URL.getOpt());
    assert connectionUrl != null;
    return RemoteOfficeManager.builder()
        .urlConnection(connectionUrl)
        .sslConfig(
            context == null ? null : context.getBeanProvider(SslConfig.class).getIfAvailable())
        .build();
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

    return Optional.ofNullable(context)
        .map(ctx -> ctx.getBeanProvider(FilterChain.class).getIfAvailable())
        .orElse(null);
  }

  private static DocumentFormatRegistry getRegistryOption(final CommandLine commandLine)
      throws IOException {

    if (commandLine.hasOption(OPT_REGISTRY.getOpt())) {
      return JsonDocumentFormatRegistry.create(
          Files.readString(
              Path.of(commandLine.getOptionValue(OPT_REGISTRY.getOpt())), StandardCharsets.UTF_8));
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

    final var options = new Options();
    options.addOption(OPT_APPLICATION_CONTEXT); // -a, --application-context
    options.addOption(OPT_CONNECTION_URL); // -c, --connection-url
    options.addOption(OPT_OUTPUT_DIRECTORY); // -d, --output-directory
    options.addOption(OPT_OUTPUT_FORMAT); // -f, --output-format
    options.addOption(OPT_HELP); // -h, --help
    options.addOption(OPT_OFFICE_HOME); // -i, --office-home
    options.addOption(OPT_KEEP_ALIVE); // -o, --keep-alive
    options.addOption(OPT_LOAD_PROPERTIES); // -l, --load-properties
    options.addOption(OPT_PROCESS_MANAGER); // -m, --process-manager
    options.addOption(OPT_HOSTNAME); // -n, --host-name
    options.addOption(OPT_OVERWRITE); // -o, --overwrite
    options.addOption(OPT_PORT); // -p, --port
    options.addOption(OPT_REGISTRY); // -r, --registry
    options.addOption(OPT_STORE_PROPERTIES); // -s, --store-properties
    options.addOption(OPT_TIMEOUT); // -u, --timeout
    options.addOption(OPT_USER_PROFILE); // -u, --user-profile
    options.addOption(OPT_VERSION); // -v, --version
    options.addOption(OPT_WORKING_DIR); // -i, --office-home
    options.addOption(OPT_EXISTING_PROCESS_ACTION); // -x, --existing-process-action
    options.addOption(OPT_PDF_PRESET); // --pdf-preset
    options.addOption(OPT_PDF_OPTION); // --pdf-option

    return options;
  }

  /**
   * Main entry point of the program.
   *
   * @param arguments program arguments.
   */
  public static void main(final String[] arguments) {

    System.exit(run(arguments));
  }

  /**
   * Runs the program, without exiting the JVM.
   *
   * @param arguments program arguments.
   * @return The exit status of the program.
   */
  /* default */
  static int run(final String... arguments) {

    try {
      final var commandLine = parse(arguments);

      // Check if the command line contains arguments that is supposed
      // to print some info and then exit.
      if (printInfoIfRequested(commandLine)) {
        return STATUS_OK;
      }

      // Get conversion arguments
      final var outputFormat = getStringOption(commandLine, OPT_OUTPUT_FORMAT.getOpt());
      final var outputDirPath = getStringOption(commandLine, OPT_OUTPUT_DIRECTORY.getOpt());
      final var registry = getRegistryOption(commandLine);
      final var overwrite = commandLine.hasOption(OPT_OVERWRITE.getOpt());
      final var filenames = commandLine.getArgs();

      // Validate arguments length
      if (outputFormat == null && filenames.length % 2 != 0 || filenames.length == 0) {
        printHelp();
        return STATUS_INVALID_ARGUMENTS;
      }

      // Build the properties and the PDF options, before anything is started, since they may
      // be invalid.
      final PdfOptions pdfOptions;
      try {
        buildProperties("load", commandLine.getOptionValues(OPT_LOAD_PROPERTIES.getOpt()));
        buildProperties("store", commandLine.getOptionValues(OPT_STORE_PROPERTIES.getOpt()));
        pdfOptions =
            PdfOptionsParser.parse(
                commandLine.getOptionValue(OPT_PDF_PRESET.getLongOpt()),
                commandLine.getOptionValues(OPT_PDF_OPTION.getLongOpt()));
      } catch (IllegalArgumentException ex) {
        printErr(ex.getMessage());
        return STATUS_INVALID_ARGUMENTS;
      }

      // Load the application context if provided
      final var context = getApplicationContextOption(commandLine);

      // Create a default office manager from the command line
      final var officeManager = createOfficeManager(commandLine, context);

      try {
        // Starts the manager
        printInfo("Starting office");
        officeManager.start();

        // Build a client converter and start the conversion
        final var converter =
            createCliConverter(commandLine, context, officeManager, registry, pdfOptions);

        if (outputFormat == null) {

          // Build 2 arrays; one containing the input files and the other
          // containing the output files.
          final var inputFilenames = new String[filenames.length / 2];
          final var outputFilenames = new String[inputFilenames.length];
          for (int i = 0, j = 0; // NOPMD - Disable for loop variables count
              i < filenames.length;
              i += 2, j++) {
            inputFilenames[j] = filenames[i];
            outputFilenames[j] = filenames[i + 1];
          }
          converter.convert(inputFilenames, outputFilenames, outputDirPath, overwrite);

        } else {

          converter.convert(filenames, outputFormat, outputDirPath, overwrite);
        }
      } finally {
        printInfo("Stopping office");
        OfficeUtils.stopQuietly(officeManager);

        // Close the application context if required
        if (context != null) {
          context.close();
        }
      }

      return STATUS_OK;

    } catch (ParseException e) {
      printErr(e.getMessage());
      printHelp();
      return STATUS_ERROR;
    } catch (Exception e) {
      printErr(e.getMessage());
      e.printStackTrace(System.err);
      return STATUS_ERROR;
    }
  }

  /**
   * Parses the arguments of the program.
   *
   * @param arguments program arguments.
   * @return The parsed command line.
   * @throws ParseException If the arguments are not valid.
   */
  /* default */
  static CommandLine parse(final String... arguments) throws ParseException {

    // A property is written -lName=value or -sName=value, and its value may contain the equal
    // sign. The name=value part is separated from its option here, since the parser would
    // otherwise split it on every equal sign.
    final var normalized = new ArrayList<String>();
    for (final var argument : arguments) {
      final var matcher = ATTACHED_PROPERTY.matcher(argument);
      if (matcher.matches()) {
        normalized.add(matcher.group(1));
        normalized.add(matcher.group(2));
      } else {
        normalized.add(argument);
      }
    }
    return new DefaultParser().parse(OPTIONS, normalized.toArray(new String[0]));
  }

  // Converts the value of a property: a boolean, an integer, or a text.
  private static Object toPropertyValue(final String value) {

    if ("true".equalsIgnoreCase(value)) {
      return Boolean.TRUE;
    }
    if ("false".equalsIgnoreCase(value)) {
      return Boolean.FALSE;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException nfe) {
      return value;
    }
  }

  // Builds the properties given as "name=value" arguments. The properties whose name starts
  // with FD are properties of the FilterData.
  private static Map<String, Object> buildProperties(final String kind, final String... args) {

    final var properties = new HashMap<String, Object>();
    if (args == null) {
      return properties;
    }

    final var filterDataProperties = new HashMap<String, Object>();
    for (final var arg : args) {
      // Only the first equal sign separates the name from the value.
      final var separator = arg.indexOf('=');
      if (separator <= 0) {
        throw new IllegalArgumentException(
            "Invalid " + kind + " property '" + arg + "'; expected name=value");
      }
      final var key = arg.substring(0, separator);
      final var value = toPropertyValue(arg.substring(separator + 1));
      if (key.length() > 2 && key.startsWith("FD")) {
        filterDataProperties.put(key.substring("FD".length()), value);
      } else {
        properties.put(key, value);
      }
    }
    if (!filterDataProperties.isEmpty()) {
      properties.put("FilterData", filterDataProperties);
    }

    return properties;
  }

  private static CliConverter createCliConverter(
      final CommandLine commandLine,
      final AbstractApplicationContext context,
      final OfficeManager officeManager,
      final DocumentFormatRegistry registry,
      final PdfOptions pdfOptions) {

    if (commandLine.hasOption(OPT_CONNECTION_URL.getOpt())) {
      final var builder = RemoteConverter.builder().officeManager(officeManager);
      if (registry != null) {
        builder.formatRegistry(registry);
      }
      return new CliConverter(builder.build(), pdfOptions);
    }

    final var builder = LocalConverter.builder().officeManager(officeManager);
    if (registry != null) {
      builder.formatRegistry(registry);
    }

    // Specify custom load properties if required
    final var loadProperties =
        buildProperties("load", commandLine.getOptionValues(OPT_LOAD_PROPERTIES.getOpt()));
    builder.loadProperties(loadProperties);

    // Specify custom store properties if required
    final var storeProperties =
        buildProperties("store", commandLine.getOptionValues(OPT_STORE_PROPERTIES.getOpt()));
    builder.storeProperties(storeProperties);

    // Specify a filter chain if required
    final var filterChain = getFilterChain(context);
    if (filterChain != null) {
      builder.filterChain(filterChain);
    }
    return new CliConverter(builder.build(), pdfOptions);
  }

  private static void printHelp() {

    final String[] help = {
      "jodconverter-cli [options] infile outfile [infile outfile ...]",
      "  or:",
      "jodconverter-cli [options] -f output-format infile [infile ...]"
    };
    final var appendable = new TextHelpAppendable(System.out);
    appendable.setMaxWidth(HELP_WIDTH);
    try {
      HelpFormatter.builder()
          .setShowSince(false)
          .setHelpAppendable(appendable)
          .get()
          .printHelp(String.join("\n", help), null, OPTIONS, null, false);
    } catch (IOException ex) {
      printErr(ex.getMessage());
    }
  }

  private static void printErr(final Object... values) {

    System.err.printf("jodconverter-cli: %s%n", values); // NOPMD - Allow System.out.println
    System.err.flush();
  }

  private static void printInfo(final String message, final Object... values) {

    System.out.printf(message, values); // NOPMD - Allow System.out.println
    System.out.println(); // NOPMD - Allow System.out.println
    System.out.flush();
  }
}
