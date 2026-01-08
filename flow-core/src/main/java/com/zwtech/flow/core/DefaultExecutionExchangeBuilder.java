package com.zwtech.flow.core;

import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;
import tools.jackson.databind.JsonNode;

/**
 * @author renc
 */
public class DefaultExecutionExchangeBuilder implements ExecutionExchange.Builder {

    private final ExecutionExchange delegate;

    private @Nullable JsonNode request;
    private @Nullable JsonNode response;
    private @Nullable VariableContext variableContext;

    public DefaultExecutionExchangeBuilder(ExecutionExchange delegate) {
        Assert.notNull(delegate, "Delegate is required");
        this.delegate = delegate;
    }

    @Override
    public ExecutionExchange.Builder request(JsonNode request) {
        this.request = request;
        return this;
    }

    @Override
    public ExecutionExchange.Builder response(JsonNode output) {
        this.response = output;
        return this;
    }

    @Override
    public ExecutionExchange.Builder variableContext(VariableContext variableContext) {
        this.variableContext = variableContext;
        return this;
    }

    @Override
    public ExecutionExchange build() {
        return new MutativeDecorator(this.delegate, this.request, this.response, this.variableContext);
    }

    private class MutativeDecorator extends ExecutionExchangeDecorator {

        private @Nullable JsonNode request;
        private @Nullable JsonNode response;
        private @Nullable VariableContext variableContext;

        public MutativeDecorator(ExecutionExchange delegate, @Nullable JsonNode request,
                @Nullable JsonNode response, @Nullable VariableContext variableContext) {
            super(delegate);
            this.request = request;
            this.response = response;
            this.variableContext = variableContext;
        }

        @Override
        public JsonNode getRequest() {
            return this.request != null ? this.request : getDelegate().getRequest();
        }

        @Override
        public JsonNode getResponse() {
            return this.response != null ? this.response : getDelegate().getResponse();
        }

        @Override
        public VariableContext getVariableContext() {
            return this.variableContext != null ? this.variableContext : getDelegate().getVariableContext();
        }
    }
}
