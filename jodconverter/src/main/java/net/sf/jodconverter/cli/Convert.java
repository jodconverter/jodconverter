package net.sf.jodconverter.cli;

import java.io.File;

import net.sf.jodconverter.OfficeDocumentConverter;
import net.sf.jodconverter.office.ManagedProcessOfficeManager;
import net.sf.jodconverter.office.OfficeManager;

/**
 * Command line interface executable.
 */
public class Convert {

    public static final int STATUS_OK = 0;
    public static final int STATUS_MISSING_INPUT_FILE = 1;
    public static final int STATUS_INVALID_ARG_COUNT = 255;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("USAGE: convert input-file output-file");
            System.exit(STATUS_INVALID_ARG_COUNT);
        }
        
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);
        if (!inputFile.isFile()) {
            System.err.println("ERROR! No such file: " + inputFile);
            System.exit(STATUS_MISSING_INPUT_FILE);
        }
        
        OfficeManager officeManager = getOfficeManager();
        officeManager.start();
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
        try {
            converter.convert(inputFile, outputFile);
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