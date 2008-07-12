package net.sf.jodconverter;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import net.sf.jodconverter.office.OfficeManager;

public class OfficeDocumentConverter {

    private final OfficeManager officeManager;
    private final DocumentFormatRegistry formatRegistry;

    private Map<String,?> defaultLoadProperties = Collections.singletonMap("Hidden", true);

    public OfficeDocumentConverter(OfficeManager officeManager) {
        this(officeManager, new DefaultDocumentFormatRegistry());
    }

    public OfficeDocumentConverter(OfficeManager officeManager, DocumentFormatRegistry formatRegistry) {
        this.officeManager = officeManager;
        this.formatRegistry = formatRegistry;
    }

    public void setDefaultLoadProperties(Map<String, ?> defaultLoadProperties) {
        this.defaultLoadProperties = defaultLoadProperties;
    }

    public DocumentFormatRegistry getFormatRegistry() {
        return formatRegistry;
    }

    public void convert(File inputFile, File outputFile) {
        String outputExtension = FilenameUtils.getExtension(outputFile.getName());
        DocumentFormat outputFormat = formatRegistry.getFormatByExtension(outputExtension);
        convert(inputFile, outputFile, outputFormat);
    }

    public void convert(File inputFile, File outputFile, DocumentFormat outputFormat) {
        String inputExtension = FilenameUtils.getExtension(inputFile.getName());
        DocumentFormat inputFormat = formatRegistry.getFormatByExtension(inputExtension);
        StandardConversionTask conversionTask = new StandardConversionTask(inputFile, outputFile, outputFormat);
        conversionTask.setDefaultLoadProperties(defaultLoadProperties);
        conversionTask.setInputFormat(inputFormat);
        officeManager.execute(conversionTask);
    }

}
