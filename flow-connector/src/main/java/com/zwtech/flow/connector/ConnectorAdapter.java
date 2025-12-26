package com.zwtech.flow.connector;

import org.example.core.serviceregistry.ServiceRegistry;
import reactor.core.publisher.Mono;

public interface ConnectorAdapter {

    boolean supports(String type);

    Mono<Void> handle(ServiceRegistry registry, RequestContext context);
}