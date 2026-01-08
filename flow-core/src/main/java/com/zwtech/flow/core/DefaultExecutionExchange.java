package com.zwtech.flow.core;

import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ExecutionExchange 的不可变实现。
 *
 * @author renc
 */
public final class DefaultExecutionExchange implements ExecutionExchange {

    private final JsonNode request;
    private final JsonNode response;
    private final VariableContext variableContext;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final @Nullable ApplicationContext applicationContext;

    public DefaultExecutionExchange(JsonNode request, JsonNode response, VariableContext variableContext) {
        this(request, response, variableContext, null);
    }

    public DefaultExecutionExchange(JsonNode request, JsonNode response, VariableContext variableContext,
            @Nullable ApplicationContext applicationContext) {
        Assert.notNull(request, "'request' is required");
        Assert.notNull(response, "'response' is required");
        Assert.notNull(variableContext, "'variableContext' is required");
        this.request = request;
        this.response = response;
        this.variableContext = variableContext;
        this.applicationContext = applicationContext;
    }

    @Override
    public VariableContext getVariableContext() {
        return this.variableContext;
    }

    @Override
    public JsonNode getRequest() {
        return this.request;
    }

    @Override
    public JsonNode getResponse() {
        return this.response;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public @Nullable ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }
}

