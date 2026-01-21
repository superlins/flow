package com.zwtech.flow.connector.filter;

import com.zwtech.flow.connector.ExecutionEnvelope;
import com.zwtech.flow.connector.RequestSpec;
import com.zwtech.flow.connector.ResponseSpec;
import reactor.core.publisher.Mono;

/**
 * @author renc
 */
public interface GlobalFilter<REQ extends RequestSpec, RESP extends ResponseSpec> {

    Mono<ExecutionEnvelope<REQ, RESP>> filter(ExecutionEnvelope<REQ, RESP> envelope,
            ConnectorFilterChain<REQ, RESP> chain);
}
