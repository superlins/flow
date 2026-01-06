package com.zwtech.flow.domain.model.apidatasource.operation;

import org.springframework.util.Assert;

import java.util.Objects;

/**
 * HTTP 操作规范
 * 描述要执行什么 HTTP 操作（path、method 等）
 * 注意：baseUrl 属于 ConnectionSpec，不在此处
 *
 * @author renc
 */
public final class HttpOperationSpec implements OperationSpec {

    private final String path;
    private final String method;

    public HttpOperationSpec(String path, String method) {
        Assert.hasText(path, "path must not be empty");
        Assert.hasText(method, "method must not be empty");
        this.path = path;
        this.method = method;
    }

    public String path() {
        return path;
    }

    public String method() {
        return method;
    }

    @Override
    public boolean sameValueAs(OperationSpec other) {
        if (!(other instanceof HttpOperationSpec o)) {
            return false;
        }
        return Objects.equals(this.path, o.path)
                && Objects.equals(this.method, o.method);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof OperationSpec other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, method);
    }
}