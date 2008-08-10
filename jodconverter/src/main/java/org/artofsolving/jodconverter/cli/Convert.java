package org.artofsolving.jodconverter.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.DocumentFormatRegistry;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.json.JsonDocumentFormatRegistry;
import org.artofsolving.jodconverter.office.ManagedProcessOfficeManager;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.artofsolving.jodconverter.util.OsUtils;
import org.json.JSONException;

/**
 * Command line interface executable.
 */
public class Convert {

    public static final int STATUS_OK = 0;
    public static final int STATUS_MISSING_INPUT_FILE = 1;
    public static final int STATUS_INVALID_ARGUMENTS = 255;

    private static final Option OPTION_OUTPUT_FORMAT = new Option("f", "output-format", true, "output format (e.g. pdf)");
    private static final Option OPTION_PORT = new Option("p", "port", true, "office socket port (optional; defaults to 8100)");
    private static final Option OPTION_REGISTRY = new Option("r", "registry", true, "document formats registry configuration file (optional)");
    private static final Options OPTIONS = initOptions();

    private static final int DEFAULT_OFFICE_PORT = 8100;

    private static Options initOptions() {
        Options options = new Options();
        options.addOption(OPTION_OUTPUT_FORMAT);
        options.addOption(OPTION_PORT);
        options.addOption(OPTION_REGISTRY);
        return options;
    }

    public static void main(String[] arguments) throws ParseException, JSONException, IOException {
        CommandLineParser commandLineParser = new PosixParser();
        CommandLine commandLine = commandLineParser.parse(OPTIONS, arguments);

        String outputFormat = null;
        if (commandLine.hasOption(OPTION_OUTPUT_FORMAT.getOpt())) {
            outputFormat = commandLine.getOptionValue(OPTION_OUTPUT_FORMAT.getOpt());
        }

        int port = DEFAULT_OFFICE_PORT;
        if (commandLine.hasOption(OPTION_PORT.getOpt())) {
            port = Integer.parseInt(commandLine.getOptionValue(OPTION_PORT.getOpt()));
        }

        String[] fileNames = commandLine.getArgs();
        if ((outputFormat == null && fileNames.length != 2) || fileNames.length < 1) {
            String syntax = "java net.sf.jodconverter.cli.Convert [options] input-file output-file; or\n"
                    + "[options] -f output-format input-file [input-file...]";
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp(syntax, OPTIONS);
            System.exit(STATUS_INVALID_ARGUMENTS);
        }
        
        DocumentFormatRegistry registry;
        if (commandLine.hasOption(OPTION_REGISTRY.getOpt())) {
            File registryFile = new File(commandLine.getOptionValue(OPTION_REGISTRY.getOpt()));
            registry = new JsonDocumentFormatRegistry(FileUtils.readFileToString(registryFile));
        } else {
            registry = new DefaultDocumentFormatRegistry();
        }
        
        OfficeManager officeManager = getOfficeManager(port);
        officeManager.start();
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager, registry);
        try {
            if (outputFormat == null) {
                File inputFile = new File(fileNames[0]);
                File outputFile = new File(fileNames[1]);
                converter.convert(inputFile, outputFile);
            } else {
                for (int i = 0; i < fileNames.length; i++) {
                    File inputFile = new File(fileNames[i]);
                    String outputName = FilenameUtils.getBaseName(fileNames[i]) + "." + outputFormat;
                    File outputFile = new File(FilenameUtils.getFullPath(fileNames[i]) + outputName);
                    converter.convert(inputFile, outputFile);
                }
            }
        } finally {
            officeManager.stop();
        }
    }

    private static OfficeManager getOfficeManager(int port) {
        String officeHome = System.getenv("OFFICE_HOME");
        if (officeHome == null) {
            //TODO try searching in standard locations
            throw new RuntimeException("Please set your OFFICE_HOME environment variable.");
        }
        String acceptString = "socket,host=127.0.0.1,port=" + port;
        return new ManagedProcessOfficeManager(new File(officeHome), guessDefaultProfileDir(), acceptString);
    }
    

    private static File guessDefaultProfileDir() {
        if (OsUtils.isWindows()) {
            return new File(System.getenv("APPDATA"), "OpenOffice.org2");
        } else {
            return new File(System.getProperty("user.home"), ".openoffice.org2");
        }
    }

}