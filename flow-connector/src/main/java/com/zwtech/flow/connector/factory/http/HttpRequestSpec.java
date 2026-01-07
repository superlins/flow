package com.zwtech.flow.connector.factory.http;

import com.zwtech.flow.connector.RequestSpec;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author renc
 */
@Builder
@Getter
public class HttpRequestSpec implements RequestSpec {

    private String url;
    private HttpMethod method;
    private HttpHeaders headers;
    private Map<String, Object> queryParams = new HashMap<>();
    private Object body;

    // retry config
    private int retries;

    private List<HttpStatus.Series> series = List.of(HttpStatus.Series.SERVER_ERROR);

    private List<HttpStatus> statuses = new ArrayList<>();

    private List<HttpMethod> methods = List.of(HttpMethod.GET);

    private List<Class<? extends Throwable>> exceptions = List.of(IOException.class, TimeoutException.class);

    private BackoffConfig backoff;

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

    @Builder
    @Getter
    public static class BackoffConfig {

        private Duration minBackoff = Duration.ofMillis(5);

        private Duration maxBackoff;

        private double multiplier = 2;

        private double jitterFactor = 0.5;

        public void validate() {
            Assert.notNull(this.minBackoff, "firstBackoff must be present");
            Assert.isTrue(jitterFactor >= 0 && jitterFactor <= 1,
                    "random factor must be between 0 and 1 (default 0.5)");
        }
    }
}
