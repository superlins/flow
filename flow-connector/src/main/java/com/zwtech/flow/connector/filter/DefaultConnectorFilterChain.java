package com.zwtech.flow.connector.filter;

import com.zwtech.flow.connector.Connector;
import com.zwtech.flow.connector.ExecutionEnvelope;
import com.zwtech.flow.connector.RequestSpec;
import com.zwtech.flow.connector.ResponseSpec;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author renc
 */
public class DefaultConnectorFilterChain<REQ extends RequestSpec, RESP extends ResponseSpec> implements ConnectorFilterChain {

    private final List<ConnectorFilter<REQ, RESP>> filters;
    private final int index;
    private final Connector<REQ, RESP> connector;

    public DefaultConnectorFilterChain(Connector<REQ, RESP> connector, List<ConnectorFilter<REQ, RESP>> filters) {
        this.connector = connector;
        this.filters = filters;
        this.index = 0;
    }

    private DefaultConnectorFilterChain(Connector<REQ, RESP> connector, DefaultConnectorFilterChain<REQ, RESP> parent, int index) {
        this.connector = connector;
        this.filters = parent.getFilters();
        this.index = index;
    }

    public List<ConnectorFilter<REQ, RESP>> getFilters() {
        return filters;
    }

    @Override
    public <REQ extends RequestSpec, RESP extends ResponseSpec> Mono<ExecutionEnvelope<REQ, RESP>> filter(ExecutionEnvelope<REQ, RESP> envelope) {
        return Mono.defer(() -> filterInternal((ExecutionEnvelope) envelope));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Mono<ExecutionEnvelope<REQ, RESP>> filterInternal(ExecutionEnvelope<REQ, RESP> envelope) {
        if (index < filters.size()) {
            ConnectorFilter<REQ, RESP> filter = (ConnectorFilter<REQ, RESP>) filters.get(index);
            return filter.filter(envelope, new DefaultConnectorFilterChain(this.connector, this, this.index + 1));
        }
        return connector.connect(envelope.requestSpec(), envelope.attributes())
                .map(envelope::withResponseSpec);
    }
}
