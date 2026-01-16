package com.zwtech.flow.core;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Default implementation of VariableContext
 * Holds request and response objects for expression parsing
 *
 * @author renc
 */
public final class DefaultVariableContext implements VariableContext {

    private final JsonNode request;
    private final JsonNode response;

    public DefaultVariableContext(JsonNode request, JsonNode response) {
        this.request = request;
        this.response = response;
    }

    public JsonNode getRequest() {
        return request;
    }

    public JsonNode getResponse() {
        return response;
    }
}
