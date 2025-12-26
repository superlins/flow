package com.zwtech.flow.connector.filter;

import org.example.core.connector.RequestSpec;
import org.example.core.connector.ResponseSpec;
import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Mono;

/**
 * @author renc
 */
public interface ConnectorFilter extends ExtensionPoint {

    Mono<ResponseSpec> filter(RequestSpec spec, ConnectorFilterChain chain);
}
