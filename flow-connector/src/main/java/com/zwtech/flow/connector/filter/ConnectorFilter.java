package com.zwtech.flow.connector.filter;

import com.zwtech.flow.connector.ExecutionEnvelope;
import com.zwtech.flow.connector.RequestSpec;
import com.zwtech.flow.connector.ResponseSpec;
import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Mono;

/**
 * @author renc
 */
public interface ConnectorFilter<REQ extends RequestSpec, RESP extends ResponseSpec> extends ExtensionPoint {
    
    Mono<ExecutionEnvelope<REQ, RESP>> filter(ExecutionEnvelope<REQ, RESP> envelope, ConnectorFilterChain chain);
}
