package ru.levitsky.helper;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility for converting a {@code Map<String, String>} to a JSON string.
 * Used to store parameters in a JSONB column.
 */
public final class JsonHelper {

    /**
     * Converts a key-value map to a JSON string
     * @param map key–value
     * @return JSON string in the format {@code {"key":"value"}}
     */
    public static String toJson(Map<String, String> map) {
        return map.entrySet().stream()
                .map(e -> "\"" + escape(e.getKey()) + "\":\"" + escape(e.getValue()) + "\"")
                .collect(Collectors.joining(",", "{", "}"));
    }

    /**
     * Escapes special characters for valid JSON
     * @param s original string
     * @return escaped string
     */
    public static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
