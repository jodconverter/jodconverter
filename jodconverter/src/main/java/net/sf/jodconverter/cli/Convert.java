package net.sf.jodconverter.cli;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;

import net.sf.jodconverter.OfficeDocumentConverter;
import net.sf.jodconverter.office.ManagedProcessOfficeManager;
import net.sf.jodconverter.office.OfficeManager;

/**
 * Command line interface executable.
 */
public class Convert {

    public static final int STATUS_OK = 0;
    public static final int STATUS_MISSING_INPUT_FILE = 1;
    public static final int STATUS_INVALID_ARGUMENTS = 255;

    private static final Option OPTION_OUTPUT_FORMAT = new Option("f", "output-format", true, "output format (e.g. pdf)");
    private static final Options OPTIONS = initOptions();

    private static Options initOptions() {
        Options options = new Options();
        options.addOption(OPTION_OUTPUT_FORMAT);
        return options;
    }

    public static void main(String[] arguments) throws ParseException {
        CommandLineParser commandLineParser = new PosixParser();
        CommandLine commandLine = commandLineParser.parse(OPTIONS, arguments);

        String outputFormat = null;
        if (commandLine.hasOption(OPTION_OUTPUT_FORMAT.getOpt())) {
            outputFormat = commandLine.getOptionValue(OPTION_OUTPUT_FORMAT.getOpt());
        }

        String[] fileNames = commandLine.getArgs();
        if ((outputFormat == null && fileNames.length != 2) || fileNames.length < 1) {
            String syntax = "java net.sf.jodconverter.cli.Convert [options] input-file output-file; or\n"
                    + "[options] -f output-format input-file [input-file...]";
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp(syntax, OPTIONS);
            System.exit(STATUS_INVALID_ARGUMENTS);
        }
        
        OfficeManager officeManager = getOfficeManager();
        officeManager.start();
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
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

    private static OfficeManager getOfficeManager() {
        String officeHome = System.getenv("OFFICE_HOME");
        if (officeHome == null) {
            //TODO try searching in standard locations
            throw new RuntimeException("Please set your OFFICE_HOME environment variable.");
        }
        return new ManagedProcessOfficeManager(new File(officeHome));
    }

}