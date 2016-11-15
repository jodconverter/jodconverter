//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
// -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
// -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter.document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A SimpleDocumentFormatRegistry contains a collection of document formats supported by office.
 */
public class SimpleDocumentFormatRegistry implements DocumentFormatRegistry {

    private Map<String, DocumentFormat> documentFormatsByExtension = new HashMap<>();
    private Map<String, DocumentFormat> documentFormatsByMediaType = new HashMap<>();

    /**
     * Add a new format to the registry.
     * 
     * @param documentFormat
     *            the format to add.
     */
    public void addFormat(DocumentFormat documentFormat) {

        // TODO: Should we check if already there ?
        documentFormatsByExtension.put(documentFormat.getExtension().toLowerCase(), documentFormat);
        documentFormatsByMediaType.put(documentFormat.getMediaType().toLowerCase(), documentFormat);
    }

    @Override
    public DocumentFormat getFormatByExtension(String extension) {

        if (extension == null) {
            return null;
        }
        return documentFormatsByExtension.get(extension.toLowerCase());
    }

    @Override
    public DocumentFormat getFormatByMediaType(String mediaType) {

        if (mediaType == null) {
            return null;
        }
        return documentFormatsByMediaType.get(mediaType.toLowerCase());
    }

    @Override
    public Set<DocumentFormat> getOutputFormats(DocumentFamily family) {

        Set<DocumentFormat> formats = new HashSet<DocumentFormat>();

        if (family != null) {
            for (DocumentFormat format : documentFormatsByExtension.values()) {
                if (format.getStoreProperties(family) != null) {
                    formats.add(format);
                }
            }
        }
        return formats;
    }

}
