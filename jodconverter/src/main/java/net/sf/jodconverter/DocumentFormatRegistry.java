package net.sf.jodconverter;

import java.util.Set;

public interface DocumentFormatRegistry {

    public DocumentFormat getFormatByExtension(String extension);

    public Set<DocumentFormat> getOutputFormats(DocumentFamily family); 

}
