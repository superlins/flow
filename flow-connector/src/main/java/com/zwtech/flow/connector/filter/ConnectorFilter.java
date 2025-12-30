package com.zwtech.flow.connector.filter;

import reactor.core.publisher.Mono;

/**
 * @author renc
 */
public interface ConnectorFilter<REQ, RESP> {
    Mono<ExecutionEnvelope<REQ, RESP>> filter(ExecutionEnvelope<REQ, RESP> envelope, ConnectorFilterChain chain);
}
