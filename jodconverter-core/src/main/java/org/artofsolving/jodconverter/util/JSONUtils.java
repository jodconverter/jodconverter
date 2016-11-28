package org.artofsolving.jodconverter.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Helper class that provides JSON utilities.
 */
public class JSONUtils {

    /**
     * Converts a JSONObject to a list.
     * 
     * @param object the JSONArray to convert.
     * @return the created list from the JSONArray object.
     */
    public static List<Object> toList(JSONArray array) {

        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    /**
     * Converts a JSONObject to a map.
     * 
     * @param object the JSONObject to convert.
     * @return the created map from the JSONObject object.
     */
    public static Map<String, Object> toMap(JSONObject object) {

        if (object == null || object == JSONObject.NULL) {
            return null;
        }

        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keySet().iterator();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }

        return map;
    }

    // Private ctor
    private JSONUtils() {
        throw new AssertionError("Utility class must not be instantiated");
    }
}
