package com.zwtech.flow.domain.model.apidatasource.connection;

import lombok.Getter;
import java.time.Duration;
import java.util.Objects;

/**
 * HTTP 连接规范
 * 描述如何连接到 HTTP 目标系统（超时、认证等）
 *
 * 根据 README.md 中的定义，支持：
 * - baseUrl: 基础 URL
 * - timeout: 超时配置（timeout, connectionTimeout, responseTimeout）
 * - retry: 重试配置（maxAttempts）
 * - rateLimiter: 限流配置
 * - cache: 缓存配置
 *
 * @author renc
 */
@Getter
public final class HttpDatasourceConnection implements DatasourceConnection {

    private final String baseUrl;
    private final Duration timeout;
    private final Duration connectionTimeout;
    private final Duration responseTimeout;
    private final Integer maxRetryAttempts;

    public HttpDatasourceConnection(String baseUrl, Duration timeout, Duration connectionTimeout, 
                                   Duration responseTimeout, Integer maxRetryAttempts) {
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.connectionTimeout = connectionTimeout;
        this.responseTimeout = responseTimeout;
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public Duration timeout() {
        return timeout;
    }

    public Duration connectionTimeout() {
        return connectionTimeout;
    }

    public Duration responseTimeout() {
        return responseTimeout;
    }

    public Integer maxRetryAttempts() {
        return maxRetryAttempts;
    }

    @Override
    public boolean sameValueAs(DatasourceConnection other) {
        if (!(other instanceof HttpDatasourceConnection o)) {
            return false;
        }
        return Objects.equals(this.baseUrl, o.baseUrl)
                && Objects.equals(this.timeout, o.timeout)
                && Objects.equals(this.connectionTimeout, o.connectionTimeout)
                && Objects.equals(this.responseTimeout, o.responseTimeout)
                && Objects.equals(this.maxRetryAttempts, o.maxRetryAttempts);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DatasourceConnection other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl, timeout, connectionTimeout, responseTimeout, maxRetryAttempts);
    }
}
