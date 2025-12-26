package com.zwtech.flow.core.server;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.List.copyOf;
import static java.util.Map.copyOf;

/**
 * @author renc
 */
public final class RequestContext {

    private final ServerRequest originalServerRequest;
    private final Map<String, Object> originalRequestBody;

    private final Map<String, Object> upstreamRequestBody;
    private final Map<String, String> upstreamRequestHeaders;
    private final Map<String, String> upstreamRequestQueryParams;
    private final Map<String, Object> upstreamRequestPathVariables;
    private final List<Object> upstreamSqlParameters;
    private final Map<String, Object> upstreamNamedSqlParameters;
    private final List<Object> upstreamRpcMethodArgs;
    private final Map<String, Object> upstreamGeneralParameters;

    // --- Expression Context ---
    // A mutable JSON node used for dynamic expression evaluation or data transformation context
    private final ObjectNode expressionContextNode;

    @Nullable
    private final Object rawUpstreamResponseData;
    private final Map<String, Object> finalClientResponseData;

    private RequestContext(Builder builder) {
        this.originalServerRequest = builder.originalServerRequest;
        this.originalRequestBody = copyOf(builder.originalRequestBody);

        this.upstreamRequestBody = copyOf(builder.upstreamRequestBody);
        this.upstreamRequestHeaders = copyOf(builder.upstreamRequestHeaders);
        this.upstreamRequestQueryParams = copyOf(builder.upstreamRequestQueryParams);
        this.upstreamRequestPathVariables = copyOf(builder.upstreamRequestPathVariables);
        this.upstreamSqlParameters = copyOf(builder.upstreamSqlParameters);
        this.upstreamNamedSqlParameters = copyOf(builder.upstreamNamedSqlParameters);
        this.upstreamRpcMethodArgs = copyOf(builder.upstreamRpcMethodArgs);
        this.upstreamGeneralParameters = copyOf(builder.upstreamGeneralParameters);

        this.expressionContextNode = builder.expressionContextNode;

        this.rawUpstreamResponseData = builder.rawUpstreamResponseData;
        this.finalClientResponseData = copyOf(builder.finalClientResponseData);
    }

    public ServerRequest getOriginalServerRequest() {
        return originalServerRequest;
    }

    public Map<String, Object> getOriginalRequestBody() {
        return originalRequestBody;
    }

    public Map<String, Object> getUpstreamRequestBody() {
        return upstreamRequestBody;
    }

    public Map<String, String> getUpstreamRequestHeaders() {
        return upstreamRequestHeaders;
    }

    public Map<String, String> getUpstreamRequestQueryParams() {
        return upstreamRequestQueryParams;
    }

    public Map<String, Object> getUpstreamRequestPathVariables() {
        return upstreamRequestPathVariables;
    }

    public List<Object> getUpstreamSqlParameters() {
        return upstreamSqlParameters;
    }

    public Map<String, Object> getUpstreamNamedSqlParameters() {
        return upstreamNamedSqlParameters;
    }

    public List<Object> getUpstreamRpcMethodArgs() {
        return upstreamRpcMethodArgs;
    }

    public Map<String, Object> getUpstreamGeneralParameters() {
        return upstreamGeneralParameters;
    }

    public ObjectNode getExpressionContextNode() {
        return expressionContextNode;
    }

    public Object getRawUpstreamResponseData() {
        return rawUpstreamResponseData;
    }

    public Map<String, Object> getFinalClientResponseData() {
        return finalClientResponseData;
    }

    public Builder mutate() {
        return Builder.from(this);
    }

    public static class Builder {

        private ServerRequest originalServerRequest;

        private final Map<String, Object> originalRequestBody = new HashMap<>();

        private final Map<String, Object> upstreamRequestBody = new HashMap<>();
        private final Map<String, String> upstreamRequestHeaders = new HashMap<>();
        private final Map<String, String> upstreamRequestQueryParams = new HashMap<>();
        private final Map<String, Object> upstreamRequestPathVariables = new HashMap<>();
        private final List<Object> upstreamSqlParameters = new ArrayList<>();
        private final Map<String, Object> upstreamNamedSqlParameters = new HashMap<>();
        private final List<Object> upstreamRpcMethodArgs = new ArrayList<>();
        private final Map<String, Object> upstreamGeneralParameters = new HashMap<>();

        private final Map<String, Object> finalClientResponseData = new HashMap<>();

        @Nullable
        private Object rawUpstreamResponseData;

        private ObjectNode expressionContextNode;

        public Builder originalServerRequest(ServerRequest originalServerRequest) {
            Assert.notNull(originalServerRequest, "originalServerRequest must not be null");
            this.originalServerRequest = originalServerRequest;
            return this;
        }

        public Builder originalRequestBody(Consumer<Map<String, Object>> originalRequestBodyConsumer) {
            Assert.notNull(originalRequestBodyConsumer, "originalRequestBody consumer must not be null");
            originalRequestBodyConsumer.accept(this.originalRequestBody);
            return this;
        }

        public Builder upstreamRequestBody(Consumer<Map<String, Object>> upstreamRequestBodyConsumer) {
            Assert.notNull(upstreamRequestBodyConsumer, "upstreamRequestBody consumer must not be null");
            upstreamRequestBodyConsumer.accept(upstreamRequestBody);
            return this;
        }

        public Builder addUpstreamRequestBody(String key, Object value) {
            this.upstreamRequestBody.put(key, value);
            return this;
        }

        public Builder upstreamRequestHeaders(Consumer<Map<String, String>> upstreamRequestHeadersConsumer) {
            Assert.notNull(upstreamRequestHeadersConsumer, "upstreamRequestHeaders consumer must not be null");
            upstreamRequestHeadersConsumer.accept(upstreamRequestHeaders);
            return this;
        }

        public Builder addUpstreamRequestHeader(String key, String value) {
            this.upstreamRequestHeaders.put(key, value);
            return this;
        }

        public Builder upstreamRequestQueryParams(Consumer<Map<String, String>> upstreamRequestQueryParamsConsumer) {
            Assert.notNull(upstreamRequestQueryParamsConsumer, "upstreamRequestQueryParams consumer must not be null");
            upstreamRequestQueryParamsConsumer.accept(upstreamRequestQueryParams);
            return this;
        }

        public Builder addUpstreamRequestQueryParam(String key, String value) {
            this.upstreamRequestQueryParams.put(key, value);
            return this;
        }

        public Builder upstreamRequestPathVariables(Consumer<Map<String, Object>> upstreamRequestPathVariablesConsumer) {
            Assert.notNull(upstreamRequestPathVariablesConsumer, "upstreamRequestPathVariables consumer must not be null");
            upstreamRequestPathVariablesConsumer.accept(upstreamRequestPathVariables);
            return this;
        }

        public Builder addUpstreamRequestPathVariable(String key, Object value) {
            this.upstreamRequestPathVariables.put(key, value);
            return this;
        }

        public Builder upstreamSqlParameters(Consumer<List<Object>> upstreamSqlParametersConsumer) {
            Assert.notNull(upstreamSqlParametersConsumer, "upstreamSqlParameters consumer must not be null");
            upstreamSqlParametersConsumer.accept(upstreamSqlParameters);
            return this;
        }

        public Builder addUpstreamSqlParameter(Object value) {
            this.upstreamSqlParameters.add(value);
            return this;
        }

        public Builder upstreamNamedSqlParameters(Consumer<Map<String, Object>> upstreamNamedSqlParametersConsumer) {
            Assert.notNull(upstreamNamedSqlParametersConsumer, "upstreamNamedSqlParameters consumer must not be null");
            upstreamNamedSqlParametersConsumer.accept(upstreamNamedSqlParameters);
            return this;
        }

        public Builder addUpstreamNamedSqlParameter(String key, Object value) {
            this.upstreamNamedSqlParameters.put(key, value);
            return this;
        }

        public Builder upstreamRpcMethodArgs(Consumer<List<Object>> upstreamRpcMethodArgsConsumer) {
            Assert.notNull(upstreamRpcMethodArgsConsumer, "upstreamRpcMethodArgs consumer must not be null");
            upstreamRpcMethodArgsConsumer.accept(upstreamRpcMethodArgs);
            return this;
        }

        public Builder addUpstreamRpcMethodArg(Object value) {
            this.upstreamRpcMethodArgs.add(value);
            return this;
        }

        public Builder upstreamGeneralParameters(Consumer<Map<String, Object>> upstreamGeneralParametersConsumer) {
            Assert.notNull(upstreamGeneralParametersConsumer, "upstreamGeneralParameters consumer must not be null");
            upstreamGeneralParametersConsumer.accept(upstreamGeneralParameters);
            return this;
        }

        public Builder addUpstreamGeneralParameter(String key, Object value) {
            this.upstreamGeneralParameters.put(key, value);
            return this;
        }

        public Builder expressionContextNode(ObjectNode expressionContextNode) {
            Assert.notNull(expressionContextNode, "expressionContextNode must not be null");
            this.expressionContextNode = expressionContextNode;
            return this;
        }

        public Builder rawUpstreamResponseData(@Nullable Object rawUpstreamResponseData) {
            this.rawUpstreamResponseData = rawUpstreamResponseData;
            return this;
        }

        public Builder finalClientResponseData(Consumer<Map<String, Object>> finalClientResponseDataConsumer) {
            Assert.notNull(finalClientResponseDataConsumer, "finalClientResponseData consumer must not be null");
            finalClientResponseDataConsumer.accept(finalClientResponseData);
            return this;
        }

        public RequestContext build() {
            return new RequestContext(this);
        }

        public static Builder from(RequestContext context) {
            Assert.notNull(context, "Cannot create a Builder from a null RequestContext.");
            return new Builder()
                    .originalServerRequest(context.getOriginalServerRequest())
                    .originalRequestBody(m -> m.putAll(context.getOriginalRequestBody()))
                    .upstreamRequestBody(m -> m.putAll(context.getUpstreamRequestBody()))
                    .upstreamRequestHeaders(m -> m.putAll(context.getUpstreamRequestHeaders()))
                    .upstreamRequestQueryParams(m -> m.putAll(context.getUpstreamRequestQueryParams()))
                    .upstreamRequestPathVariables(m -> m.putAll(context.getUpstreamRequestPathVariables()))
                    .upstreamSqlParameters(list -> list.addAll(context.getUpstreamSqlParameters()))
                    .upstreamNamedSqlParameters(m -> m.putAll(context.getUpstreamNamedSqlParameters()))
                    .upstreamRpcMethodArgs(list -> list.addAll(context.getUpstreamRpcMethodArgs()))
                    .upstreamGeneralParameters(m -> m.putAll(context.getUpstreamGeneralParameters()))
                    .expressionContextNode(context.getExpressionContextNode())
                    .rawUpstreamResponseData(context.getRawUpstreamResponseData())
                    .finalClientResponseData(m -> m.putAll(context.getFinalClientResponseData()));
        }
    }
}