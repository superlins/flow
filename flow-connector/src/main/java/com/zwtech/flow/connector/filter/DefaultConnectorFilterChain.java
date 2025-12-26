package com.zwtech.flow.connector.filter;

import org.example.core.connector.Connector;
import org.example.core.connector.RequestSpec;
import org.example.core.connector.ResponseSpec;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author renc
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DefaultConnectorFilterChain implements ConnectorFilterChain {

    private final List<ConnectorFilter> filters;
    private final int index;
    private final Connector connector;

    public DefaultConnectorFilterChain(Connector connector, List<ConnectorFilter> filters) {
        this.connector = connector;
        this.filters = filters;
        this.index = 0;
    }

    private DefaultConnectorFilterChain(Connector connector, DefaultConnectorFilterChain parent, int index) {
        this.connector = connector;
        this.filters = parent.getFilters();
        this.index = index;
    }

    public List<ConnectorFilter> getFilters() {
        return filters;
    }

    @Override
    public Mono<ResponseSpec> filter(RequestSpec spec) {
        return Mono.defer(() -> {
            if (index < filters.size()) {
                ConnectorFilter filter = filters.get(index);
                return filter.filter(spec, new DefaultConnectorFilterChain(this.connector, this, this.index + 1));
            }
            return connector.connect(spec);
        });
    }
}
