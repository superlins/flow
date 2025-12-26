package com.zwtech.flow.connector.filter;

import org.example.core.connector.RequestSpec;
import org.example.core.connector.ResponseSpec;
import reactor.core.publisher.Mono;

/**
 * @author renc
 */
public interface ConnectorFilterChain {

    Mono<ResponseSpec> filter(RequestSpec spec);
}
