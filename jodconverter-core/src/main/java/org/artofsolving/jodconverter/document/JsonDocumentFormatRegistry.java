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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.artofsolving.jodconverter.util.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A JsonDocumentFormatRegistry contains a collection of {@code DocumentFormat} supported by office
 * that has been loaded loaded from a JSON source.
 */
public class JsonDocumentFormatRegistry extends SimpleDocumentFormatRegistry {

    /**
     * Creates a JsonDocumentFormatRegistry from the given InputStream.
     * 
     * @param source
     *            the InputStream (JSON format) containing the DocumentFormat collection.
     * @return the created JsonDocumentFormatRegistry.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public static JsonDocumentFormatRegistry create(InputStream source) throws IOException {

        return create(IOUtils.toString(source, "UTF-8"));
    }

    /**
     * Creates a JsonDocumentFormatRegistry from the given source.
     * 
     * @param source
     *            the string (JSON format) containing the DocumentFormat collection.
     * @return the created JsonDocumentFormatRegistry.
     */
    public static JsonDocumentFormatRegistry create(String source) {

        JsonDocumentFormatRegistry registry = new JsonDocumentFormatRegistry();
        registry.readJsonArray(source);
        return registry;
    }

    // Force static function call
    private JsonDocumentFormatRegistry() {}

    // Fill the registry from the given JSON source
    private void readJsonArray(String source) {

        JSONArray array = new JSONArray(source);
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonFormat = array.getJSONObject(i);
            DocumentFormat format = new DocumentFormat(jsonFormat.getString("name"), jsonFormat.getString("extension"), jsonFormat.getString("mediaType"));
            if (jsonFormat.has("inputFamily")) {
                format.setInputFamily(DocumentFamily.valueOf(jsonFormat.getString("inputFamily")));
            }
            if (jsonFormat.has("loadProperties")) {
                format.setLoadProperties(JSONUtils.toMap(jsonFormat.getJSONObject("loadProperties")));
            }
            if (jsonFormat.has("storePropertiesByFamily")) {
                JSONObject jsonStorePropertiesByFamily = jsonFormat.getJSONObject("storePropertiesByFamily");
                for (String key : JSONObject.getNames(jsonStorePropertiesByFamily)) {
                    Map<String, ?> storeProperties = JSONUtils.toMap(jsonStorePropertiesByFamily.getJSONObject(key));
                    format.setStoreProperties(DocumentFamily.valueOf(key), storeProperties);
                }
            }
            addFormat(format);
        }
    }

}
