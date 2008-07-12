package net.sf.jodconverter;

import static org.testng.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import net.sf.jodconverter.office.OfficeManager;
import net.sf.jodconverter.office.SingleOfficeManager;

import org.apache.commons.io.FilenameUtils;
import org.testng.annotations.Test;

@Test(groups="functional")
public class OfficeConverterFunctionalTests {

    private static final File OFFICE_HOME = new File("/home/mirko/apps/openoffice.org-2.4.0");
    private static final String CONNECT_STRING = "socket,host=127.0.0.1,port=8100";

    public void convertAll() throws IOException {
        OfficeManager officeManager = new SingleOfficeManager(OFFICE_HOME, CONNECT_STRING);
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
        DocumentFormatRegistry formatRegistry = converter.getFormatRegistry();
        
        officeManager.start();
        try {
            File dir = new File("src/test/resources/documents");
            for (File inputFile : dir.listFiles()) {
                String inputExtension = FilenameUtils.getExtension(inputFile.getName());
                DocumentFormat inputFormat = formatRegistry.getFormatByExtension(inputExtension);
                Set<DocumentFormat> outputFormats = formatRegistry.getOutputFormats(inputFormat.getInputFamily());
                for (DocumentFormat outputFormat : outputFormats) {
                    File outputFile = File.createTempFile("test", "." + outputFormat.getExtension());
                    System.out.printf("-- converting %s to %s... ", inputFormat.getExtension(), outputFormat.getExtension());
                    converter.convert(inputFile, outputFile, outputFormat);
                    System.out.printf("done.\n");
                    assertTrue(outputFile.isFile() && outputFile.length() > 0);
                    //TODO use file detection to make sure outputFile is in the expected format
                }
            }
        } finally {
            officeManager.stop();
        }
    }

}
