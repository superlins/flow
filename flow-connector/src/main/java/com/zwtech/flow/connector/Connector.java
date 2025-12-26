package com.zwtech.flow.connector;

import reactor.core.publisher.Mono;

/**
 * @author renc
 */
@FunctionalInterface
public interface Connector<REQ extends RequestSpec, RESP extends ResponseSpec> {

    Mono<RESP> connect(REQ request);
}