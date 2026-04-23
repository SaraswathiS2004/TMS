package com.tms.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tms.data.dto.People;

public class JackSonUtils {

    public static <T> T deSerialize(String json , Class<T> clazz ) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json , clazz);
    }
    public static String serialize(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(object);
        return jsonString;
    }
}
