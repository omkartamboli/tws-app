package com.trading.app.tradingapp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonSerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSerializer.class);

    public static final String serialize(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not serialize the object.", e);
            return e.getMessage();
        }
    }
}
