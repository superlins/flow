package com.zwtech.flow.connector.filter.mapping;// package org.example.core.connector.filter.mapping;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.example.core.connector.filter.ConnectorFilterChain;
// import org.example.core.expression.ExpressionEngine;
// import org.example.core.server.ServerUpstreamRequest;
// import org.example.core.server.ServerUpstreamResponse;
// import org.example.core.serviceregistry.ServiceRegistry;
// import reactor.core.publisher.Mono;
//
// import static org.example.core.connector.ConnectorConstants.SERVICE_REGISTRY_ATTR;
//
// /**
//  * @author renc
//  */
// public class ResponseMappingFilter extends AbstractMappingFilter {
//
//     public static final int RESPONSE_MAPPING_FILTER_ORDER = - 1;
//
//     public ResponseMappingFilter(ObjectMapper objectMapper, ExpressionEngine expressionEngine) {
//         super(objectMapper, expressionEngine);
//     }
//
//     @Override
//     public Mono<ServerUpstreamResponse> filter(ServerUpstreamRequest request, ConnectorFilterChain chain) {
//         return chain.filter(request)
//                 .flatMap(response -> Mono.fromCallable(() -> {
//
//                     var context = request.getContextObject();
//                     var serviceRegistry = request.getOriginalServerRequest()
//                             .exchange().<ServiceRegistry>getRequiredAttribute(SERVICE_REGISTRY_ATTR);
//
//                     var builder = response.mutate();
//
//                     applyMappings(serviceRegistry.getResponseMappings(), context, (rule, value) -> {
//                         switch (rule.getMappingType()) {
//                             case HEADER -> builder.header(rule.getName(), String.valueOf(value));
//                             case BODY -> builder.body(rule.getName(), value);
//                         }
//                     });
//
//                     return builder.build();
//                 }));
//     }
//
//     @Override
//     public int getOrder() {
//         return RESPONSE_MAPPING_FILTER_ORDER;
//     }
// }
