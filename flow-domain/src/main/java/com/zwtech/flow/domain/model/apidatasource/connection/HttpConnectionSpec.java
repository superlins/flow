package com.zwtech.flow.domain.model.apidatasource.connection;

import org.springframework.util.Assert;

import java.util.Objects;

/**
 * HTTP 连接规范
 * 描述如何连接到 HTTP 目标系统（baseUrl、超时、认证等）
 *
 * @author renc
 */
public final class HttpConnectionSpec implements ConnectionSpec {

    private final String baseUrl;
    private final Integer connectTimeout;
    private final Integer readTimeout;

    public HttpConnectionSpec(String baseUrl) {
        this(baseUrl, null, null);
    }

    public HttpConnectionSpec(String baseUrl, Integer connectTimeout, Integer readTimeout) {
        Assert.hasText(baseUrl, "baseUrl must not be empty");
        this.baseUrl = baseUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public Integer connectTimeout() {
        return connectTimeout;
    }

    public Integer readTimeout() {
        return readTimeout;
    }

    @Override
    public boolean sameValueAs(ConnectionSpec other) {
        if (!(other instanceof HttpConnectionSpec o)) {
            return false;
        }
        return Objects.equals(this.baseUrl, o.baseUrl)
                && Objects.equals(this.connectTimeout, o.connectTimeout)
                && Objects.equals(this.readTimeout, o.readTimeout);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ConnectionSpec other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl, connectTimeout, readTimeout);
    }
}
