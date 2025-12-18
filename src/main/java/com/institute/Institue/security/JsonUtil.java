package com.institute.Institue.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public class JsonUtil {
    private static final ObjectMapper M = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return M.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> fromJsonToMap(String json) {
        try {
            return M.readValue(json, new TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            return null;
        }
    }
}

