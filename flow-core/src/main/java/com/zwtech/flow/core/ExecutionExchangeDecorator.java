package com.zwtech.flow.core;

import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * @author renc
 */
public class ExecutionExchangeDecorator implements ExecutionExchange {

    private final ExecutionExchange delegate;

    public ExecutionExchangeDecorator(ExecutionExchange delegate) {
        Assert.notNull(delegate, "ExecutionExchange 'delegate' is required.");
        this.delegate = delegate;
    }

    public ExecutionExchange getDelegate() {
        return delegate;
    }


    @Override
    public VariableContext getVariableContext() {
        return getDelegate().getVariableContext();
    }

    @Override
    public JsonNode getRequest() {
        return getDelegate().getRequest();
    }

    @Override
    public JsonNode getResponse() {
        return getDelegate().getResponse();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return getDelegate().getAttributes();
    }

    @Override
    public @Nullable ApplicationContext getApplicationContext() {
        return getDelegate().getApplicationContext();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [delegate=" + getDelegate() + "]";
    }
}
