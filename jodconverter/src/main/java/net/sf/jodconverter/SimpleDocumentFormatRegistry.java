package net.sf.jodconverter;

import java.util.ArrayList;
import java.util.List;

public class SimpleDocumentFormatRegistry implements DocumentFormatRegistry {

	private List<DocumentFormat> documentFormats = new ArrayList<DocumentFormat>();

	public void addFormat(DocumentFormat documentFormat) {
		documentFormats.add(documentFormat);
	}

	public DocumentFormat getFormatByExtension(String extension) {
        if (extension == null) {
            return null;
        }
        String lowerExtension = extension.toLowerCase();
        //TODO create a documentByExtension map instead
		for (DocumentFormat format : documentFormats) {
			if (format.getExtension().equals(lowerExtension)) {
				return format;
			}
		}
		return null;
	}

}
