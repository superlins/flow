package com.zwtech.flow.connector.filter;

import org.example.core.connector.RequestSpec;
import org.example.core.connector.ResponseSpec;
import org.example.core.connector.factory.http.HttpRequestSpec;
import org.example.core.connector.factory.http.HttpResponseSpec;
import org.springframework.core.Ordered;
import reactor.core.publisher.Mono;

/**
 * @author renc
 */
public class OrderedConnectorFilter implements ConnectorFilter, Ordered {

    private final ConnectorFilter delegate;

    private final int order;

    public OrderedConnectorFilter(ConnectorFilter delegate, int order) {
        this.delegate = delegate;
        this.order = order;
    }

    public ConnectorFilter getDelegate() {
        return delegate;
    }

    @Override
    public Mono<ResponseSpec> filter(RequestSpec context, ConnectorFilterChain chain) {
        var httpRequestSpec = new HttpRequestSpec();
        // omit setter
        return this.delegate.filter(httpRequestSpec, chain).doOnNext(responseSpec -> {
            var httpResponseSpec = (HttpResponseSpec) responseSpec;
        });
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public String toString() {
        return "OrderedConnectorFilter{" + "delegate=" + delegate + ", order=" + order + '}';
    }
}
