package com.zwtech.flow.connector.filter;

import reactor.core.publisher.Mono;

/**
 * @author renc
 */
public interface ConnectorFilterChain {

    <REQ, RESP> Mono<ExecutionEnvelope<REQ, RESP>> filter(ExecutionEnvelope<REQ, RESP> envelope);
}
