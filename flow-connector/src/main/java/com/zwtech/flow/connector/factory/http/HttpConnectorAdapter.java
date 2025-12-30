package com.zwtech.flow.connector.factory.http;

import com.zwtech.flow.connector.ConnectorAdapter;
import com.zwtech.flow.connector.DataBinder;
import com.zwtech.flow.connector.ExecutionAttributes;
import com.zwtech.flow.connector.ExecutionEnvelope;
import com.zwtech.flow.connector.factory.ConnectorEndpointTypeNames;
import com.zwtech.flow.connector.filter.DefaultConnectorFilterChain;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * @author renc
 */
public class HttpConnectorAdapter implements ConnectorAdapter {

    private final HttpConnectorFactory connectorFactory;

    public HttpConnectorAdapter(HttpConnectorFactory connectorFactory) {
        this.connectorFactory = connectorFactory;
    }

    public boolean supports(String type) {
        return ConnectorEndpointTypeNames.HTTP.equals(type);
    }

    // public Mono<Void> handle(ServiceRegistry registry, RequestContext context) {
    //
    //     var httpProperties = registry.getHttp();
    //     var requestConfig = httpProperties.getRequestConfig();
    //
    //     var httpConnectorConfig = new HttpConnectorConfig();
    //     httpConnectorConfig.setConnectionTimeout(requestConfig.getConnectionTimeout());
    //     httpConnectorConfig.setResponseTimeout(requestConfig.getResponseTimeout());
    //     httpConnectorConfig.setRetryDisabled(requestConfig.isRetryDisabled());
    //     httpConnectorConfig.setCompressionEnabled(requestConfig.isCompressionEnabled());
    //     httpConnectorConfig.setCertVerifyDisabled(requestConfig.isCertVerifyDisabled());
    //
    //     var connector = connectorFactory.newInstance(httpConnectorConfig);
    //
    //     var httpRequestSpec = new HttpRequestSpec();
    //     httpRequestSpec.setUrl(httpProperties.getUrl());
    //     httpRequestSpec.setMethod(httpProperties.getMethod());
    //     var httpHeaders = new HttpHeaders();
    //     httpProperties.getHeaders().forEach(httpHeaders::add);
    //     httpRequestSpec.setHeaders(httpHeaders);
    //     httpRequestSpec.setQueryParams(httpProperties.getQueryParams());
    //     httpRequestSpec.setBody(httpProperties.getBody());
    //
    //     var retryConfig = httpProperties.getRetryConfig();
    //     if (retryConfig.isEnabled() && retryConfig.getRetries() > 0) {
    //         httpRequestSpec.setRetries(retryConfig.getRetries());
    //         httpRequestSpec.setSeries(retryConfig.getSeries());
    //         httpRequestSpec.setStatuses(retryConfig.getStatuses());
    //         httpRequestSpec.setMethods(retryConfig.getMethods());
    //         httpRequestSpec.setExceptions(retryConfig.getExceptions());
    //
    //         var backoff = httpRequestSpec.getBackoff();
    //         backoff.setMinBackoff(retryConfig.getMinBackoff());
    //         backoff.setMaxBackoff(retryConfig.getMaxBackoff());
    //         backoff.setMultiplier(retryConfig.getMultiplier());
    //         backoff.setJitterFactor(retryConfig.getJitterFactor());
    //     }
    //
    //     httpRequestSpec.setTimeout(httpProperties.getTimeout());
    //     httpRequestSpec.setAttributes(httpProperties.getAttributes());
    //
    //     // 自定义过滤器
    //     // serviceRegistry.getCustomFilters().stream()
    //     //         .sorted(Comparator.comparingInt(ServiceDefinition.FilterConfig::getOrder))
    //     //         .forEach(filterConfig -> {
    //     //             DynamicFilterFactory factory = dynamicFilterFactories.get(filterConfig.getFilterType());
    //     //             if (factory != null) {
    //     //                 filters.add(factory.createFilter(filterConfig.getParameters(), objectMapper));
    //     //             } else {
    //     //                 System.err.println("Warning: Unknown dynamic filter type configured: " + filterConfig.getFilterType());
    //     //             }
    //     //         });
    //
    //     var chain = new DefaultConnectorFilterChain(connector, List.of());
    //
    //     return chain.filter(httpRequestSpec).doOnNext(responseSpec -> {
    //
    //         var httpResponseSpec = (HttpResponseSpec) responseSpec;
    //
    //         // TODO(renc): switch to reactor context view
    //         var context1 = RequestContext.from(context).$response(response -> {
    //             response.put("body", httpResponseSpec.getBody());
    //             response.put("headers", httpResponseSpec.getHeaders());
    //             response.put("status", httpResponseSpec.getStatusCode());
    //         }).build();
    //
    //         var vars = registry.getVars();
    //     }).then();
    // }

    @Override
    public Mono<ExecutionExchange> execute(ExecutionExchange exchange, ApiDatasource datasource) {
        var requestSpec = DataBinder.bind(exchange, HttpRequestSpec.class);

        var envelope = new ExecutionEnvelope<HttpRequestSpec, HttpResponseSpec>() {
            private final HttpRequestSpec req = requestSpec;
            private HttpResponseSpec resp;

            @Override
            public HttpRequestSpec requestSpec() { return req; }

            @Override
            public Optional<HttpResponseSpec> responseSpec() { return Optional.ofNullable(resp); }

            @Override
            public ExecutionAttributes attributes() { return new ExecutionAttributes() {
                @Override
                public <T> Optional<T> get(String key) { return Optional.empty(); }
                @Override
                public ExecutionAttributes with(String key, Object value) { return this; }
            }; }

            @Override
            public ExecutionEnvelope<HttpRequestSpec, HttpResponseSpec> withRequestSpec(HttpRequestSpec requestSpec) { return this; }

            @Override
            public ExecutionEnvelope<HttpRequestSpec, HttpResponseSpec> withResponseSpec(HttpResponseSpec responseSpec) {
                this.resp = responseSpec;
                return this;
            }

            @Override
            public ExecutionEnvelope<HttpRequestSpec, HttpResponseSpec> withAttributes(ExecutionAttributes attributes) { return this; }
        };

        var connector = connectorFactory.create(datasource);

        // 自定义过滤器
        // serviceRegistry.getCustomFilters().stream()
        //         .sorted(Comparator.comparingInt(ServiceDefinition.FilterConfig::getOrder))
        //         .forEach(filterConfig -> {
        //             DynamicFilterFactory factory = dynamicFilterFactories.get(filterConfig.getFilterType());
        //             if (factory != null) {
        //                 filters.add(factory.createFilter(filterConfig.getParameters(), objectMapper));
        //             } else {
        //                 System.err.println("Warning: Unknown dynamic filter type configured: " + filterConfig.getFilterType());
        //             }
        //         });

        var chain = new DefaultConnectorFilterChain(connector, List.of());

        // Execute the filter chain, final connector execution is at the end
        return chain.filter(envelope)
                .map(env -> {
                    var responseSpec = DataBinder.bind(exchange, HttpResponseSpec.class);
                    // TODO(renc): context rewrite
                    return exchange;
                });
    }
}
