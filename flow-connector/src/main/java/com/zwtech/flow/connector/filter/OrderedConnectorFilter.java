package com.zwtech.flow.connector.filter;

import com.zwtech.flow.connector.ExecutionEnvelope;
import com.zwtech.flow.connector.RequestSpec;
import com.zwtech.flow.connector.ResponseSpec;
import org.springframework.core.Ordered;
import reactor.core.publisher.Mono;

/**
 * @author renc
 */
public class OrderedConnectorFilter<REQ extends RequestSpec, RESP extends ResponseSpec>
        implements ConnectorFilter<REQ, RESP>, Ordered {

    private final ConnectorFilter<REQ, RESP> delegate;

    private final int order;

    public OrderedConnectorFilter(ConnectorFilter<REQ, RESP> delegate, int order) {
        this.delegate = delegate;
        this.order = order;
    }

    public ConnectorFilter<REQ, RESP> getDelegate() {
        return delegate;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public String toString() {
        return "OrderedConnectorFilter{" + "delegate=" + delegate + ", order=" + order + '}';
    }

    @Override
    public Mono<ExecutionEnvelope<REQ, RESP>> filter(ExecutionEnvelope<REQ, RESP> envelope,
            ConnectorFilterChain<REQ, RESP> chain) {
        return this.delegate.filter(envelope, chain);
    }
}
