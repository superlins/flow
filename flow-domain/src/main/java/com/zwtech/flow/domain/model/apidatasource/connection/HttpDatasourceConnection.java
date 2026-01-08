package com.zwtech.flow.domain.model.apidatasource.connection;

import java.util.Objects;

/**
 * HTTP 连接规范
 * 描述如何连接到 HTTP 目标系统（超时、认证等）
 *
 * @author renc
 */
public final class HttpDatasourceConnection implements DatasourceConnection {

    private final Integer connectTimeout;
    private final Integer readTimeout;

    public HttpDatasourceConnection(Integer connectTimeout, Integer readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public Integer connectTimeout() {
        return connectTimeout;
    }

    public Integer readTimeout() {
        return readTimeout;
    }

    @Override
    public boolean sameValueAs(DatasourceConnection other) {
        if (!(other instanceof HttpDatasourceConnection o)) {
            return false;
        }
        return Objects.equals(this.connectTimeout, o.connectTimeout)
                && Objects.equals(this.readTimeout, o.readTimeout);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DatasourceConnection other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectTimeout, readTimeout);
    }
}
