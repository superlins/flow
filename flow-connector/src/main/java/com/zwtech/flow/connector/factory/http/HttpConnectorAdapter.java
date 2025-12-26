package com.zwtech.flow.connector.factory.http;

import org.example.core.connector.ConnectorAdapter;
import org.example.core.connector.RequestContext;
import org.example.core.connector.factory.ConnectorEndpointTypeNames;
import org.example.core.connector.filter.DefaultConnectorFilterChain;
import org.example.core.serviceregistry.ServiceRegistry;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author renc
 */
public class HttpConnectorAdapter implements ConnectorAdapter {

    private final HttpConnectorFactory connectorFactory;

    public HttpConnectorAdapter(HttpConnectorFactory connectorFactory) {
        this.connectorFactory = connectorFactory;
    }

    @Override
    public boolean supports(String type) {
        return ConnectorEndpointTypeNames.HTTP.equals(type);
    }

    @Override
    public Mono<Void> handle(ServiceRegistry registry, RequestContext context) {

        var httpProperties = registry.getHttp();
        var requestConfig = httpProperties.getRequestConfig();

        var httpConnectorConfig = new HttpConnectorConfig();
        httpConnectorConfig.setConnectionTimeout(requestConfig.getConnectionTimeout());
        httpConnectorConfig.setResponseTimeout(requestConfig.getResponseTimeout());
        httpConnectorConfig.setRetryDisabled(requestConfig.isRetryDisabled());
        httpConnectorConfig.setCompressionEnabled(requestConfig.isCompressionEnabled());
        httpConnectorConfig.setCertVerifyDisabled(requestConfig.isCertVerifyDisabled());

        var connector = connectorFactory.newInstance(httpConnectorConfig);

        var httpRequestSpec = new HttpRequestSpec();
        httpRequestSpec.setUrl(httpProperties.getUrl());
        httpRequestSpec.setMethod(httpProperties.getMethod());
        var httpHeaders = new HttpHeaders();
        httpProperties.getHeaders().forEach(httpHeaders::add);
        httpRequestSpec.setHeaders(httpHeaders);
        httpRequestSpec.setQueryParams(httpProperties.getQueryParams());
        httpRequestSpec.setBody(httpProperties.getBody());

        var retryConfig = httpProperties.getRetryConfig();
        if (retryConfig.isEnabled() && retryConfig.getRetries() > 0) {
            httpRequestSpec.setRetries(retryConfig.getRetries());
            httpRequestSpec.setSeries(retryConfig.getSeries());
            httpRequestSpec.setStatuses(retryConfig.getStatuses());
            httpRequestSpec.setMethods(retryConfig.getMethods());
            httpRequestSpec.setExceptions(retryConfig.getExceptions());

            var backoff = httpRequestSpec.getBackoff();
            backoff.setMinBackoff(retryConfig.getMinBackoff());
            backoff.setMaxBackoff(retryConfig.getMaxBackoff());
            backoff.setMultiplier(retryConfig.getMultiplier());
            backoff.setJitterFactor(retryConfig.getJitterFactor());
        }

        httpRequestSpec.setTimeout(httpProperties.getTimeout());
        httpRequestSpec.setAttributes(httpProperties.getAttributes());

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

        return chain.filter(httpRequestSpec).doOnNext(responseSpec -> {

            var httpResponseSpec = (HttpResponseSpec) responseSpec;

            // TODO(renc): switch to reactor context view
            var context1 = RequestContext.from(context).$response(response -> {
                response.put("body", httpResponseSpec.getBody());
                response.put("headers", httpResponseSpec.getHeaders());
                response.put("status", httpResponseSpec.getStatusCode());
            }).build();

            var vars = registry.getVars();
        }).then();
    }
}
