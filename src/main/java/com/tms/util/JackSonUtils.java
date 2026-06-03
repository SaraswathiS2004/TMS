package com.tms.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JackSonUtils {

    // Shared, configured mapper. Ignore unknown properties so read-only/computed fields the
    // client echoes back (e.g. BudgetItem.remaining) don't break deserialization.
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> T deSerialize(String json, Class<T> clazz) throws JsonProcessingException {
        return MAPPER.readValue(json, clazz);
    }

    public static String serialize(Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }
}
