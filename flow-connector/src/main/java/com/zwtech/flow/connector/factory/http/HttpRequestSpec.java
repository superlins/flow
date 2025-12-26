package com.zwtech.flow.connector.factory.http;

import org.example.core.connector.RequestSpec;
import org.springframework.core.style.ToStringCreator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * @author renc
 */
public class HttpRequestSpec implements RequestSpec {

    private String url;
    private HttpMethod method;
    private HttpHeaders headers;
    private Map<String, Object> queryParams = new HashMap<>();
    private Object body;

    // retry config
    private int retries = 0;

    private List<HttpStatus.Series> series = List.of(HttpStatus.Series.SERVER_ERROR);

    private List<HttpStatus> statuses = new ArrayList<>();

    private List<HttpMethod> methods = List.of(HttpMethod.GET);

    private List<Class<? extends Throwable>> exceptions = List.of(IOException.class, TimeoutException.class);

    private BackoffConfig backoff = new BackoffConfig();

    // all-in response timeout
    private Duration timeout;

    private Map<String, Object> attributes = new HashMap<>();

    public void validate() {
        Assert.isTrue(this.retries > 0, "retries must be greater than 0");
        Assert.isTrue(!this.series.isEmpty() || !this.statuses.isEmpty() || !this.exceptions.isEmpty(),
                "series, status and exceptions may not all be empty");
        Assert.notEmpty(this.methods, "methods may not be empty");
        if (this.backoff != null) {
            this.backoff.validate();
        }
        if (this.timeout != null) {
            Assert.isTrue(!timeout.isNegative(), "timeout should be >= 0");
        }
    }

    public String getUrl() {
        return url;
    }

    public HttpRequestSpec setUrl(String url) {
        this.url = url;
        return this;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public HttpRequestSpec setMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpRequestSpec setHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public HttpRequestSpec setQueryParams(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
        return this;
    }

    public Object getBody() {
        return body;
    }

    public HttpRequestSpec setBody(Object body) {
        this.body = body;
        return this;
    }

    public int getRetries() {
        return retries;
    }

    public HttpRequestSpec setRetries(int retries) {
        this.retries = retries;
        return this;
    }

    public List<HttpStatus.Series> getSeries() {
        return series;
    }

    public HttpRequestSpec setSeries(List<HttpStatus.Series> series) {
        this.series = series;
        return this;
    }

    public List<HttpStatus> getStatuses() {
        return statuses;
    }

    public HttpRequestSpec setStatuses(List<HttpStatus> statuses) {
        this.statuses = statuses;
        return this;
    }

    public List<HttpMethod> getMethods() {
        return methods;
    }

    public HttpRequestSpec setMethods(List<HttpMethod> methods) {
        this.methods = methods;
        return this;
    }

    public List<Class<? extends Throwable>> getExceptions() {
        return exceptions;
    }

    public HttpRequestSpec setExceptions(List<Class<? extends Throwable>> exceptions) {
        this.exceptions = exceptions;
        return this;
    }

    public BackoffConfig getBackoff() {
        return backoff;
    }

    public HttpRequestSpec setBackoff(BackoffConfig backoff) {
        this.backoff = backoff;
        return this;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public HttpRequestSpec setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public HttpRequestSpec setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) getAttributes().get(name);
    }

    public <T> Optional<T> getAttribute(String name, Class<T> type) {
        Object value = getAttributes().get(name);
        return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
    }

    public static class BackoffConfig {

        private Duration minBackoff = Duration.ofMillis(5);

        private Duration maxBackoff;

        private double multiplier = 2;

        private double jitterFactor = 0.5;

        public BackoffConfig() {
        }

        public BackoffConfig(Duration minBackoff, Duration maxBackoff, double multiplier, double jitterFactor) {
            this.minBackoff = minBackoff;
            this.maxBackoff = maxBackoff;
            this.multiplier = multiplier;
            this.jitterFactor = jitterFactor;
        }

        public void validate() {
            Assert.notNull(this.minBackoff, "firstBackoff must be present");
            Assert.isTrue(jitterFactor >= 0 && jitterFactor <= 1,
                    "random factor must be between 0 and 1 (default 0.5)");
        }

        public Duration getMinBackoff() {
            return minBackoff;
        }

        public void setMinBackoff(Duration minBackoff) {
            this.minBackoff = minBackoff;
        }

        public Duration getMaxBackoff() {
            return maxBackoff;
        }

        public void setMaxBackoff(Duration maxBackoff) {
            this.maxBackoff = maxBackoff;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }

        public double getJitterFactor() {
            return jitterFactor;
        }

        public BackoffConfig setJitterFactor(double jitterFactor) {
            this.jitterFactor = jitterFactor;
            return this;
        }

        @Override
        public String toString() {
            return new ToStringCreator(this).append("firstBackoff", minBackoff)
                    .append("maxBackoff", maxBackoff)
                    .append("multiplier", multiplier)
                    .append("jitterFactor", jitterFactor)
                    .toString();
        }

    }
}
