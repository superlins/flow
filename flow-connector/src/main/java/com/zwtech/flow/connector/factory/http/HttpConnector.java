package com.zwtech.flow.connector.factory.http;

import com.zwtech.flow.connector.Connector;
import com.zwtech.flow.connector.ExecutionAttributes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.function.Predicate;

/**
 * @author renc
 */
@Slf4j
public class HttpConnector implements Connector<HttpRequestSpec, HttpResponseSpec> {

    private final WebClient webClient;

    public HttpConnector(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<HttpResponseSpec> connect(HttpRequestSpec spec, ExecutionAttributes attributes) {
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

        var httpResponseMono = headersSpec.exchangeToMono(resp -> resp.bodyToMono(JsonNode.class)
                .map(body -> HttpResponseSpec.builder()
                        .body(body)
                        .headers(resp.headers().asHttpHeaders())
                        .statusCode(resp.statusCode())
                        .build()));

        httpResponseMono = applyRetry(spec, httpResponseMono);

        if (spec.getTimeout() != null) {
            httpResponseMono = httpResponseMono.timeout(spec.getTimeout());
        }

        return httpResponseMono.onErrorMap(ex -> new RuntimeException("connect error", ex));
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

    private static void retryLog(Retry.RetrySignal retrySignal, HttpRequestSpec spec) {
        log.warn("Retrying request (attempt {}/{})", retrySignal.totalRetries() + 1, spec.getRetries());
    }

    private boolean requiresBody(HttpMethod method) {
        return method.equals(HttpMethod.PUT) || method.equals(HttpMethod.POST) || method.equals(HttpMethod.PATCH);
    }
}
