//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2008 - Mirko Nasato <mirko@artofsolving.com>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, you can find it online
// at http://www.gnu.org/licenses/lgpl-2.1.html.
//
package org.artofsolving.jodconverter;

import java.util.HashMap;
import java.util.Map;

public class DocumentFormat {

    private String name;
    private String extension;
    private String mediaType;
    private DocumentFamily inputFamily;
    private Map<String,?> loadProperties;
    private Map<DocumentFamily,Map<String,?>> storePropertiesByFamily;

    public DocumentFormat() {
        // default
    }

    public DocumentFormat(String name, String extension, String mediaType) {
        this.name = name;
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public DocumentFamily getInputFamily() {
        return inputFamily;
    }

    public void setInputFamily(DocumentFamily documentFamily) {
        this.inputFamily = documentFamily;
    }

    public Map<String, ?> getLoadProperties() {
        return loadProperties;
    }

    public void setLoadProperties(Map<String,?> loadProperties) {
        this.loadProperties = loadProperties;
    }

    public Map<DocumentFamily, Map<String, ?>> getStorePropertiesByFamily() {
        return storePropertiesByFamily;
    }

    public void setStorePropertiesByFamily(Map<DocumentFamily, Map<String,?>> storePropertiesByFamily) {
        this.storePropertiesByFamily = storePropertiesByFamily;
    }

    public void setStoreProperties(DocumentFamily family, Map<String,?> storeProperties) {
        if (storePropertiesByFamily == null) {
            storePropertiesByFamily = new HashMap<DocumentFamily,Map<String,?>>();
        }
        storePropertiesByFamily.put(family, storeProperties);
    }

    public Map<String,?> getStoreProperties(DocumentFamily family) {
        if (storePropertiesByFamily == null) {
            return null;
        }
        return storePropertiesByFamily.get(family);
    }

}
