package com.zwtech.flow.connector.factory.http;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.example.core.connector.Connector;
import org.example.core.connector.factory.AbstractConnectorFactory;
import org.example.core.connector.factory.ConnectorEndpointTypeNames;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * @author renc
 */
public class HttpConnectorFactory extends AbstractConnectorFactory<HttpConnectorConfig, HttpRequestSpec, HttpResponseSpec> {

    private final Map<HttpConnectorConfig, WebClient> clientCache = new ConcurrentHashMap<>();

    private final WebClient.Builder webClientBuilder;

    public HttpConnectorFactory(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public String name() {
        return ConnectorEndpointTypeNames.HTTP;
    }

    @Override
    public Connector<HttpRequestSpec, HttpResponseSpec> newInstance(HttpConnectorConfig config) {

        if (config == null) {
            throw new IllegalArgumentException("HttpConnectorConfig must not be null");
        }

        var webClient = clientCache.computeIfAbsent(config, this::createWebClient);

        return spec -> {

            spec.validate();

            var bodySpec = webClient.method(spec.getMethod()).uri(uriBuilder -> {
                final var builder = uriBuilder.path(spec.getUrl());
                spec.getQueryParams().forEach(builder::queryParam);
                return builder.build();
            }).headers(httpHeaders -> {
                if (spec.getHeaders() != null) {
                    httpHeaders.addAll(spec.getHeaders());
                }
            });

            WebClient.RequestHeadersSpec<?> headersSpec;
            if (requiresBody(spec.getMethod()) && spec.getBody() != null) {
                headersSpec = bodySpec.bodyValue(spec.getBody());
            } else {
                headersSpec = bodySpec;
            }

            var httpResponseMono = headersSpec.retrieve()
                    .toEntity(byte[].class)
                    .map(responseEntity -> new HttpResponseSpec().setBody(responseEntity.getBody())
                            .setHeaders(responseEntity.getHeaders())
                            .setStatusCode(responseEntity.getStatusCode()));

            httpResponseMono = applyRetry(spec, httpResponseMono);

            if (spec.getTimeout() != null) {
                httpResponseMono = httpResponseMono.timeout(spec.getTimeout());
            }

            return httpResponseMono.onErrorMap(ex -> new RuntimeException("connect error", ex));
        };
    }

    private static Mono<HttpResponseSpec> applyRetry(HttpRequestSpec spec, Mono<HttpResponseSpec> httpResponseMono) {
        if (spec.getRetries() > 0) {
            Predicate<Throwable> retryCondition = throwable -> {
                HttpMethod requestMethod = spec.getMethod();
                boolean isMethodRetryable = spec.getMethods().contains(requestMethod);
                if (!isMethodRetryable) {
                    return false;
                }

                if (throwable instanceof WebClientResponseException responseException) {
                    HttpStatus statusCode = HttpStatus.resolve(responseException.getStatusCode().value());
                    boolean isStatusRetryable = false;
                    if (statusCode != null) {
                        if (spec.getStatuses().contains(statusCode)) {
                            isStatusRetryable = true;
                        } else {
                            for (HttpStatus.Series series : spec.getSeries()) {
                                if (statusCode.series().equals(series)) {
                                    isStatusRetryable = true;
                                    break;
                                }
                            }
                        }
                    }

                    return isStatusRetryable;
                }

                for (Class<? extends Throwable> retryableExceptionClass : spec.getExceptions()) {
                    if (retryableExceptionClass.isInstance(throwable) || (throwable.getCause() != null && retryableExceptionClass.isInstance(throwable.getCause()))) {
                        return true;
                    }
                }

                return false;
            };

            Retry retrySpec;

            var backoffConfig = spec.getBackoff();
            if (backoffConfig != null) {
                retrySpec = Retry.backoff(spec.getRetries(), backoffConfig.getMinBackoff())
                        .maxBackoff(backoffConfig.getMaxBackoff())
                        .multiplier(backoffConfig.getMultiplier())
                        .jitter(backoffConfig.getJitterFactor())
                        .filter(retryCondition)
                        .doBeforeRetry(retrySignal -> retryLog(retrySignal, spec));
            } else {
                retrySpec = Retry.max(spec.getRetries())
                        .filter(retryCondition)
                        .doBeforeRetry(retrySignal -> retryLog(retrySignal, spec));
            }

            httpResponseMono = httpResponseMono.retryWhen(retrySpec);
        }
        return httpResponseMono;
    }

    private WebClient createWebClient(HttpConnectorConfig config) {
        var connectionProvider = ConnectionProvider.builder("flowHttpClient").maxConnections(200).build();

        var httpClient = HttpClient.create(connectionProvider)
                .compress(config.isCompressionEnabled())
                .disableRetry(config.isRetryDisabled())
                .responseTimeout(config.getResponseTimeout())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) config.getConnectionTimeout().toMillis());

        if (config.isCertVerifyDisabled()) {
            try {
                SslContext sslContext = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();

                httpClient = httpClient.secure(spec -> spec.sslContext(sslContext));
            } catch (Exception e) {
                LOGGER.error("Could not configure HTTP Client SSL self signed strategy", e);
            }
        }

        return webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 增加内存限制
                .build();
    }

    private static void retryLog(Retry.RetrySignal retrySignal, HttpRequestSpec spec) {
        LOGGER.warn("Retrying request (attempt {}/{})", retrySignal.totalRetries() + 1, spec.getRetries());
    }

    private boolean requiresBody(HttpMethod method) {
        return method.equals(HttpMethod.PUT) || method.equals(HttpMethod.POST) || method.equals(HttpMethod.PATCH);
    }
}
