package com.zwtech.flow.core;

import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import tools.jackson.databind.JsonNode;

import java.util.Map;

/**
 * @author renc
 */
public interface ExecutionExchange {

    /**
     * Return the current request object.
     */
    JsonNode getRequest();

    /**
     * Return the current response object.
     */
    JsonNode getResponse();

    /**
     * Return a mutable map of request attributes for the current exchange.
     */
    Map<String, Object> getAttributes();

    @SuppressWarnings("unchecked")
    default <T> @Nullable T getAttribute(String name) {
        return (T) getAttributes().get(name);
    }

    default <T> T getRequiredAttribute(String name) {
        T value = getAttribute(name);
        Assert.notNull(value, () -> "Required attribute '" + name + "' is missing");
        return value;
    }

    @SuppressWarnings("unchecked")
    default <T> T getAttributeOrDefault(String name, T defaultValue) {
        return (T) getAttributes().getOrDefault(name, defaultValue);
    }

    /**
     * Return the {@link ApplicationContext} associated with the web application,
     * if it was initialized with one via
     */
    @Nullable ApplicationContext getApplicationContext();

    default Builder mutate() {
        return new DefaultExecutionExchangeBuilder(this);
    }

    interface Builder {
        Builder request(JsonNode request);
        Builder response(JsonNode response);
        ExecutionExchange build();
    }
}
