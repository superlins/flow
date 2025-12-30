package com.zwtech.flow.domain.model.apidatasource;

import org.springframework.util.Assert;

import java.net.URI;

public final class DatasourceEndpoint {

    private final DatasourceProtocol protocol;
    private final URI uri;
    private final String method;

    public DatasourceEndpoint(DatasourceProtocol protocol,
                              URI uri,
                              String method) {
        Assert.notNull(protocol, "protocol must not be null");
        Assert.notNull(uri, "uri must not be null");
        Assert.hasText(method, "method must not be blank");

        this.protocol = protocol;
        this.uri = uri;
        this.method = method.toUpperCase();
    }

    public DatasourceProtocol protocol() {
        return protocol;
    }

    public URI uri() {
        return uri;
    }

    public String method() {
        return method;
    }
}