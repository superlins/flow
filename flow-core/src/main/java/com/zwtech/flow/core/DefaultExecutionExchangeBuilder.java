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
    public ExecutionExchange build() {
        return new MutativeDecorator(this.delegate, this.request, this.response);
    }

    private class MutativeDecorator extends ExecutionExchangeDecorator {

        private @Nullable JsonNode request;
        private @Nullable JsonNode response;

        public MutativeDecorator(ExecutionExchange delegate, @Nullable JsonNode request, @Nullable JsonNode response) {
            super(delegate);
            this.request = request;
            this.response = response;
        }

        @Override
        public JsonNode getRequest() {
            return request != null ? request : getDelegate().getRequest();
        }

        @Override
        public JsonNode getResponse() {
            return response != null ? response : getDelegate().getResponse();
        }
    }
}
