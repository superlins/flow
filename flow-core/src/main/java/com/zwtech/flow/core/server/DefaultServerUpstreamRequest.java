package com.zwtech.flow.core.server;

import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.core.connector.ConnectorConstants.MAPPED_CONTEXT_ATTR;

/**
 * @author renc
 */
public class DefaultServerUpstreamRequest implements ServerUpstreamRequest {

    private final ServerRequest originalRequest;
    private final Map<String, Object> body = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> queryParams = new HashMap<>();
    private final Map<String, Object> pathVariables = new HashMap<>();
    private final List<Object> sqlParameters  = new ArrayList<>();
    private final Map<String, Object> namedSqlParameters  = new HashMap<>();
    private final List<Object> rpcMethodArgs = new ArrayList<>();
    private final Map<String, Object> generalParameters  = new HashMap<>();

    public DefaultServerUpstreamRequest(ServerRequest originalRequest) {
        Assert.notNull(originalRequest, "'originalRequest' is required");
        this.originalRequest = originalRequest;
    }

    @Override
    public ServerRequest getOriginalServerRequest() {
        return this.originalRequest;
    }

    @Override
    public Object getContextObject() {
        return originalRequest.exchange().getRequiredAttribute(MAPPED_CONTEXT_ATTR);
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
