package com.zwtech.flow.domain.model.apidatasource.behavior;

import java.time.Duration;
import java.util.Map;

/**
 * HTTP 操作规范
 *
 * @author renc
 */
public final class HttpOperationBehavior implements OperationBehavior {

    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final Map<String, Object> queryParams;

    private final Map<String, Object> requestBody;
    private final Map<String, Object> responseBody;
    private final Duration timeout;

    public HttpOperationBehavior(String url, String method, Map<String, String> headers, Map<String, Object> queryParams,
            Map<String, Object> requestBody, Map<String, Object> responseBody, Duration timeout) {
        this.url = url;
        this.method = method;
        this.headers = headers;
        this.queryParams = queryParams;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.timeout = timeout;
    }

    public String url() {
        return url;
    }

    public String method() {
        return method;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public Map<String, Object> queryParams() {
        return queryParams;
    }

    public Map<String, Object> requestBody() {
        return requestBody;
    }

    public Map<String, Object> responseBody() {
        return responseBody;
    }

    public Duration timeout() {
        return timeout;
    }

    @Override
    public boolean sameValueAs(OperationBehavior other) {
        return false;
    }
}