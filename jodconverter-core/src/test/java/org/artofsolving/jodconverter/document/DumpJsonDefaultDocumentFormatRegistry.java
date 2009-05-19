//
// JODConverter - Java OpenDocument Converter
// Copyright 2009 Art of Solving Ltd
// Copyright 2004-2009 Mirko Nasato
//
// JODConverter is free software: you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
//
// JODConverter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General
// Public License along with JODConverter.  If not, see
// <http://www.gnu.org/licenses/>.
//
package org.artofsolving.jodconverter.document;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.artofsolving.jodconverter.ReflectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Exectable class that dumps a JSON version of the {@link DefaultDocumentFormatRegistry}
 */
class DumpJsonDefaultDocumentFormatRegistry {

    private static class SortedJsonObject extends JSONObject {
         public SortedJsonObject() {
             try {
                 Field field = JSONObject.class.getDeclaredField("myHashMap");
                 field.setAccessible(true);
                 field.set(this, new LinkedHashMap<String,Object>());
             } catch (Exception exception) {
                 // pass; will not be sorted
             }
        }
    }

    private static JSONObject toJson(DocumentFormat format) throws JSONException {
        JSONObject jsonFormat = new SortedJsonObject();
        jsonFormat.put("name", format.getName());
        jsonFormat.put("extension", format.getExtension());
        jsonFormat.put("mediaType", format.getMediaType());
        if (format.getInputFamily() != null) {
            jsonFormat.put("inputFamily", format.getInputFamily().name());
        }
        if (format.getLoadProperties() != null) {
            jsonFormat.put("loadProperties", toJson(format.getLoadProperties()));
        }
        if (format.getStorePropertiesByFamily() != null) {
            JSONObject jsonStorePropertiesByFamily = new SortedJsonObject();
            for (Map.Entry<DocumentFamily,Map<String,?>> entry : format.getStorePropertiesByFamily().entrySet()) {
                jsonStorePropertiesByFamily.put(entry.getKey().name(), toJson(entry.getValue()));
            }
            jsonFormat.put("storePropertiesByFamily", jsonStorePropertiesByFamily);
        }
        return jsonFormat;
    }
    
    private static JSONObject toJson(Map<String,?> properties) throws JSONException {
        JSONObject jsonProperties = new SortedJsonObject();
        for (Map.Entry<String,?> entry : properties.entrySet()) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String,?> jsonValue = (Map<String,?>) entry.getValue();
                jsonProperties.put(entry.getKey(), toJson(jsonValue));
            } else {
                jsonProperties.put(entry.getKey(), entry.getValue());
            }
        }
        return jsonProperties;
    }

    public static void main(String[] args) throws Exception {
        DefaultDocumentFormatRegistry registry = new DefaultDocumentFormatRegistry();
        @SuppressWarnings("unchecked")
        List<DocumentFormat> formats = (List<DocumentFormat>) ReflectionUtils.getPrivateField(SimpleDocumentFormatRegistry.class, registry, "documentFormats");
        JSONArray array = new JSONArray();
        for (DocumentFormat format : formats) {
            array.put(toJson(format));
        }
        System.out.println(array.toString(2));
    }

}
