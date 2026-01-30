package ru.levitsky.helper;

import java.util.Map;
import java.util.stream.Collectors;

public final class JsonHelper {

    public static String toJson(Map<String, String> map) {
        return map.entrySet().stream()
                .map(e -> "\"" + escape(e.getKey()) + "\":\"" + escape(e.getValue()) + "\"")
                .collect(Collectors.joining(",", "{", "}"));
    }

    public static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
