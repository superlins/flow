package com.zwtech.flow.core.server;

import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.example.core.connector.ConnectorConstants.MAPPED_CONTEXT_ATTR;

/**
 * @author renc
 */
class DefaultServerUpstreamRequestBuilder implements ServerUpstreamRequest.Builder {

    private final ServerRequest originalRequest;
    private final Map<String, Object> body = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> queryParams = new HashMap<>();
    private final Map<String, Object> pathVariables = new HashMap<>();
    private final List<Object> sqlParameters  = new ArrayList<>();
    private final Map<String, Object> namedSqlParameters  = new HashMap<>();
    private final List<Object> rpcMethodArgs = new ArrayList<>();
    private final Map<String, Object> generalParameters  = new HashMap<>();

    public DefaultServerUpstreamRequestBuilder(ServerUpstreamRequest original) {
        Assert.notNull(original, "ServerUpstreamRequest is required");
        this.originalRequest = original.getOriginalServerRequest();
        this.body.putAll(original.getBody());
        this.headers.putAll(original.getHeaders());
        this.queryParams.putAll(original.getQueryParams());
        this.pathVariables.putAll(original.getPathVariables());
        this.sqlParameters.addAll(original.getSqlParameters());
        this.namedSqlParameters.putAll(original.getNamedSqlParameters());
        this.rpcMethodArgs.addAll(original.getRpcMethodArgs());
        this.generalParameters.putAll(original.getGeneralParameters());
    }

    @Override
    public ServerUpstreamRequest.Builder body(Consumer<Map<String, Object>> bodyConsumer) {
        Assert.notNull(bodyConsumer, "bodyConsumer must not be null");
        bodyConsumer.accept(this.body);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder body(String key, Object value) {
        this.body.put(key, value);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder headers(Consumer<Map<String, String>> headersConsumer) {
        Assert.notNull(headersConsumer, "headersConsumer must not be null");
        headersConsumer.accept(this.headers);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder queryParams(Consumer<Map<String, String>> queryParamsConsumer) {
        Assert.notNull(queryParamsConsumer, "queryParamsConsumer must not be null");
        queryParamsConsumer.accept(this.queryParams);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder queryParam(String key, String value) {
        this.queryParams.put(key, value);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder pathVariables(Consumer<Map<String, Object>> pathVariablesConsumer) {
        Assert.notNull(pathVariablesConsumer, "pathVariablesConsumer must not be null");
        pathVariablesConsumer.accept(this.pathVariables);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder pathVariable(String key, Object value) {
        this.pathVariables.put(key, value);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder sqlParameters(Consumer<List<Object>> sqlParametersConsumer) {
        Assert.notNull(sqlParametersConsumer, "sqlParametersConsumer must not be null");
        sqlParametersConsumer.accept(this.sqlParameters);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder sqlParameter(Object value) {
        this.sqlParameters.add(value);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder namedSqlParameters(Consumer<Map<String, Object>> namedSqlParametersConsumer) {
        Assert.notNull(namedSqlParametersConsumer, "namedSqlParametersConsumer must not be null");
        namedSqlParametersConsumer.accept(this.namedSqlParameters);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder namedSqlParameter(String key, Object value) {
        this.namedSqlParameters.put(key, value);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder rpcMethodArgs(Consumer<List<Object>> rpcMethodArgsConsumer) {
        Assert.notNull(rpcMethodArgsConsumer, "rpcMethodArgsConsumer must not be null");
        rpcMethodArgsConsumer.accept(this.rpcMethodArgs);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder rpcMethodArg(Object value) {
        this.rpcMethodArgs.add(value);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder generalParameters(Consumer<Map<String, Object>> generalParametersConsumer) {
        Assert.notNull(generalParametersConsumer, "generalParametersConsumer must not be null");
        generalParametersConsumer.accept(this.generalParameters);
        return this;
    }

    @Override
    public ServerUpstreamRequest.Builder generalParameter(String key, Object value) {
        this.generalParameters.put(key, value);
        return this;
    }

    @Override
    public ServerUpstreamRequest build() {
        return new MutatedServerUpstreamRequest(this.originalRequest,
                Map.copyOf(this.body),
                Map.copyOf(this.headers),
                Map.copyOf(this.queryParams),
                Map.copyOf(this.pathVariables),
                List.copyOf(this.sqlParameters),
                Map.copyOf(this.namedSqlParameters),
                List.copyOf(this.rpcMethodArgs),
                Map.copyOf(this.generalParameters));
    }

    private static class MutatedServerUpstreamRequest implements ServerUpstreamRequest {

        private final ServerRequest originalRequest;
        private final Map<String, Object> body;
        private final Map<String, String> headers;
        private final Map<String, String> queryParams;
        private final Map<String, Object> pathVariables;
        private final List<Object> sqlParameters;
        private final Map<String, Object> namedSqlParameters ;
        private final List<Object> rpcMethodArgs;
        private final Map<String, Object> generalParameters ;

        public MutatedServerUpstreamRequest(ServerRequest originalRequest, Map<String, Object> body, Map<String, String> headers, Map<String, String> queryParams, Map<String, Object> pathVariables, List<Object> sqlParameters, Map<String, Object> namedSqlParameters, List<Object> rpcMethodArgs, Map<String, Object> generalParameters) {
            this.originalRequest = originalRequest;
            this.body = body;
            this.headers = headers;
            this.queryParams = queryParams;
            this.pathVariables = pathVariables;
            this.sqlParameters = sqlParameters;
            this.namedSqlParameters = namedSqlParameters;
            this.rpcMethodArgs = rpcMethodArgs;
            this.generalParameters = generalParameters;
        }

        @Override
        public ServerRequest getOriginalServerRequest() {
            return this.originalRequest;
        }

        @Override
        public Object getContextObject() {
            return this.originalRequest.exchange().getRequiredAttribute(MAPPED_CONTEXT_ATTR);
        }

        @Override
        public Map<String, Object> getBody() {
            return this.body;
        }

        @Override
        public Map<String, String> getHeaders() {
            return this.headers;
        }

        @Override
        public Map<String, String> getQueryParams() {
            return this.queryParams;
        }

        @Override
        public Map<String, Object> getPathVariables() {
            return this.pathVariables;
        }

        @Override
        public List<Object> getSqlParameters() {
            return this.sqlParameters;
        }

        @Override
        public Map<String, Object> getNamedSqlParameters() {
            return this.namedSqlParameters;
        }

        @Override
        public List<Object> getRpcMethodArgs() {
            return this.rpcMethodArgs;
        }

        @Override
        public Map<String, Object> getGeneralParameters() {
            return this.generalParameters;
        }
    }
}
