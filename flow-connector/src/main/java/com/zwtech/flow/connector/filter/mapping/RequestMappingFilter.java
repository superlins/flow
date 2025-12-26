package com.zwtech.flow.connector.filter.mapping;// package org.example.core.connector.filter.mapping;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.example.core.connector.filter.ConnectorFilterChain;
// import org.example.core.expression.ExpressionEngine;
// import org.example.core.server.ServerUpstreamRequest;
// import org.example.core.server.ServerUpstreamResponse;
// import org.example.core.serviceregistry.ServiceRegistry;
// import org.springframework.core.Ordered;
// import reactor.core.publisher.Mono;
//
// import static org.example.core.connector.ConnectorConstants.SERVICE_REGISTRY_ATTR;
//
// /**
//  * @author renc
//  */
// public class RequestMappingFilter extends AbstractMappingFilter {
//
//     public static final int REQUEST_MAPPING_FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE + 100;
//
//     public RequestMappingFilter(ObjectMapper objectMapper, ExpressionEngine expressionEngine) {
//         super(objectMapper, expressionEngine);
//     }
//
//     @Override
//     public Mono<ServerUpstreamResponse> filter(ServerUpstreamRequest request, ConnectorFilterChain chain) {
//         return Mono.defer(() -> {
//
//             var context = request.getContextObject();
//             var serviceRegistry = request.getOriginalServerRequest()
//                     .exchange().<ServiceRegistry>getRequiredAttribute(SERVICE_REGISTRY_ATTR);
//
//             var builder = request.mutate();
//
//             applyMappings(serviceRegistry.getRequestMappings(), context, (rule, value) -> {
//                 switch (rule.getMappingType()) {
//                     case GENERAL -> builder.generalParameter(rule.getName(), value);
//                     case URI_VARIABLE -> builder.pathVariable(rule.getName(), value);
//                     case QUERY_PARAM -> builder.queryParam(rule.getName(), String.valueOf(value));
//                     case HEADER -> builder.header(rule.getName(), String.valueOf(value));
//                     case BODY -> builder.body(rule.getName(), value);
//                 }
//             });
//
//             return chain.filter(builder.build());
//         });
//     }
//
//     @Override
//     public int getOrder() {
//         return REQUEST_MAPPING_FILTER_ORDER;
//     }
// }
