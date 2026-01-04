package com.zwtech.flow.domain.model.apidatasource.operation;

import org.springframework.util.Assert;

import java.util.Objects;

public final class HttpOperationSpec implements OperationSpec {

    private final String baseUrl;
    private final String path;
    private final String method;

    public HttpOperationSpec(String baseUrl, String path, String method) {
        Assert.hasText(baseUrl, "baseUrl must not be empty");
        Assert.hasText(path, "path must not be empty");
        Assert.hasText(method, "method must not be empty");
        this.baseUrl = baseUrl;
        this.path = path;
        this.method = method;
    }

    @Override
    public boolean sameValueAs(OperationSpec other) {
        if (!(other instanceof HttpOperationSpec o)) return false;
        return Objects.equals(this.baseUrl, o.baseUrl)
                && Objects.equals(this.path, o.path)
                && Objects.equals(this.method, o.method);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof OperationSpec other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl, path, method);
    }
}