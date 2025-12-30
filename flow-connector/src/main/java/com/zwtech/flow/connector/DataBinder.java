package com.zwtech.flow.connector;

import tools.jackson.databind.JsonNode;

/**
 * @author renc
 */
public abstract class DataBinder {

    public static <T> T bind(JsonNode schema, JsonNode data, Class<T> c) {
        return null;
    }

    public static <T> T bind(JsonNode n, Class<T> c) {
        return null;
    }
}
