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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.document.JsonDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.remote.RemoteConverter;
import org.jodconverter.remote.office.RemoteOfficeManager;
import org.jodconverter.remote.ssl.SslConfig;

/** Command line interface executable. */
@SuppressWarnings("PMD.UseUtilityClass")
public final class Convert {

  public static final int STATUS_OK = 0;
  // public static final int STATUS_MISSING_INPUT_FILE = 1;
  public static final int STATUS_ERROR = 2;
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
  private static final Option OPT_DISABLE_OPENGL =
      Option.builder("g").longOpt("disable-opengl").desc("Disable OpenGL (optional)").build();
  private static final Option OPT_HELP =
      Option.builder("h").longOpt("help").desc("displays help at the command prompt").build();
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
  private static final Option OPT_LOAD_PROPERTIES =
      Option.builder("l")
          .longOpt("load-properties")
          .valueSeparator()
          .hasArgs()
          .desc("load properties (optional; eg. -lPassword=myPassword)")
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
  private static final Option OPT_STORE_PROPERTIES =
      Option.builder("s")
          .longOpt("store-properties")
          .valueSeparator()
          .hasArgs()
          .desc("store properties (optional; eg. -sOverwrite=true -sFDPageRange=1-2)")
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
      Option.builder("v").longOpt("version").desc("displays version information and exit").build();

  private static final Options OPTIONS = initOptions();

  private static void checkPrintInfoAndExit(final CommandLine commandLine) {

    if (commandLine.hasOption(OPT_HELP.getOpt())) {
      printHelp();
      System.exit(STATUS_OK);
    }

    if (commandLine.hasOption(OPT_VERSION.getOpt())) {
      final Package pack = Convert.class.getPackage();
      printInfo("jodconverter-cli version %s", pack.getImplementationVersion());
      System.exit(STATUS_OK);
    }
  }

  private static OfficeManager createOfficeManager(
      final CommandLine commandLine, final AbstractApplicationContext context) {

    // If the URL is present, we will use the remote office manager and thus,
    // an office installation won't be required locally.
    if (commandLine.hasOption(OPT_CONNECTION_URL.getOpt())) {
      final String connectionUrl = getStringOption(commandLine, OPT_CONNECTION_URL.getOpt());
      assert connectionUrl != null;
      return RemoteOfficeManager.builder()
          .urlConnection(connectionUrl)
          .sslConfig(context == null ? null : context.getBean(SslConfig.class))
          .build();
    }

    // Not remote conversion...

    final LocalOfficeManager.Builder builder = LocalOfficeManager.builder();

    if (commandLine.hasOption(OPT_OFFICE_HOME.getOpt())) {
      builder.officeHome(commandLine.getOptionValue(OPT_OFFICE_HOME.getOpt()));
    }

    builder.killExistingProcess(commandLine.hasOption(OPT_KILL_EXISTING_PROCESS.getOpt()));

    builder.disableOpengl(commandLine.hasOption(OPT_DISABLE_OPENGL.getOpt()));

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

  @Nullable
  private static AbstractApplicationContext getApplicationContextOption(
      final CommandLine commandLine) {

    if (commandLine.hasOption(OPT_APPLICATION_CONTEXT.getOpt())) {

      return new FileSystemXmlApplicationContext(
          commandLine.getOptionValue(OPT_APPLICATION_CONTEXT.getOpt()));
    }

    return null;
  }

  @Nullable
  private static FilterChain getFilterChain(final ApplicationContext context) {

    return Optional.ofNullable(context).map(ctx -> ctx.getBean(FilterChain.class)).orElse(null);
  }

  @Nullable
  private static DocumentFormatRegistry getRegistryOption(final CommandLine commandLine)
      throws IOException {

    if (commandLine.hasOption(OPT_REGISTRY.getOpt())) {
      return JsonDocumentFormatRegistry.create(
          FileUtils.readFileToString(
              new File(commandLine.getOptionValue(OPT_REGISTRY.getOpt())), "UTF-8"));
    }

    return null;
  }

  @Nullable
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
    options.addOption(OPT_LOAD_PROPERTIES);
    options.addOption(OPT_DISABLE_OPENGL);
    options.addOption(OPT_PROCESS_MANAGER);
    options.addOption(OPT_OVERWRITE);
    options.addOption(OPT_PORT);
    options.addOption(OPT_REGISTRY);
    options.addOption(OPT_STORE_PROPERTIES);
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
      final OfficeManager officeManager = createOfficeManager(commandLine, context);

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

      System.exit(STATUS_OK);

    } catch (ParseException e) {
      printErr(e.getMessage());
      printHelp();
      System.exit(STATUS_ERROR);
    } catch (Exception e) {
      printErr(e.getMessage());
      e.printStackTrace(System.err);
      System.exit(STATUS_ERROR);
    }
  }

  @Nullable
  private static Map<String, Object> toMap(final String... options) {

    if (options.length % 2 != 0) {
      return null;
    }

    return IntStream.range(0, options.length)
        .filter(i -> i % 2 == 0)
        .boxed()
        .collect(
            Collectors.toMap(
                i -> options[i],
                i -> {
                  final String val = options[i + 1];
                  if ("true".equalsIgnoreCase(val)) {
                    return Boolean.TRUE;
                  }
                  if ("false".equalsIgnoreCase(val)) {
                    return Boolean.FALSE;
                  }
                  try {
                    return Integer.parseInt(val);
                  } catch (NumberFormatException nfe) {
                    return val;
                  }
                }));
  }

  @Nullable
  private static Map<String, Object> buildProperties(@Nullable final String... args) {

    if (args == null || args.length == 0) {
      return null;
    }

    final Map<String, Object> argsMap = toMap(args);
    if (argsMap == null) {
      return null;
    }

    final Map<String, Object> properties = new HashMap<>();
    final Map<String, Object> filterDataProperties = new HashMap<>();
    for (final Map.Entry<String, Object> entry : argsMap.entrySet()) {
      final String key = entry.getKey();
      if (key.length() > 2 && key.startsWith("FD")) {
        filterDataProperties.put(key.substring("FD".length()), entry.getValue());
      } else {
        properties.put(key, entry.getValue());
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
      @Nullable final DocumentFormatRegistry registry) {

    if (commandLine.hasOption(OPT_CONNECTION_URL.getOpt())) {
      final RemoteConverter.Builder builder =
          RemoteConverter.builder().officeManager(officeManager);
      if (registry != null) {
        builder.formatRegistry(registry);
      }
      return new CliConverter(builder.build());
    }

    final LocalConverter.Builder builder = LocalConverter.builder().officeManager(officeManager);
    if (registry != null) {
      builder.formatRegistry(registry);
    }

    // Specify custom load properties if required
    final Map<String, Object> loadProperties =
        buildProperties(commandLine.getOptionValues(OPT_LOAD_PROPERTIES.getOpt()));
    if (loadProperties != null) {
      // Ensure the default properties will be applied.
      final Map<String, Object> props = new HashMap<>(LocalConverter.DEFAULT_LOAD_PROPERTIES);
      props.putAll(loadProperties);
      builder.loadProperties(props);
    }

    // Specify custom store properties if required
    final Map<String, Object> storeProperties =
        buildProperties(commandLine.getOptionValues(OPT_STORE_PROPERTIES.getOpt()));
    if (storeProperties != null) {
      builder.storeProperties(storeProperties);
    }

    // Specify a filter chain if required
    final FilterChain filterChain = getFilterChain(context);
    if (filterChain != null) {
      builder.filterChain(filterChain);
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

  private static void printErr(final Object... values) {

    System.err.println( // NOPMD - Allow System.err.println
        String.format("jodconverter-cli: %s", values));
    System.err.flush();
  }

  private static void printInfo(final String message, final Object... values) {

    System.out.println( // NOPMD - Allow System.out.println
        String.format(message, values));
    System.out.flush();
  }
}
