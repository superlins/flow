package com.zwtech.flow.connector;

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author renc
 */
public final class RequestContext {

    private final Map<String, Object> $request;
    private final Map<String, Object> $response;
    private final Map<String, Object> $vars;

    // TODO(renc): built-in vars, e.g. $now/$date/$time

    private RequestContext(Builder builder) {
        this.$request = Map.copyOf(builder.$request);
        this.$response = Map.copyOf(builder.$response);
        this.$vars = Map.copyOf(builder.$vars);
    }

    public Map<String, Object> get$request() {
        return $request;
    }

    public Map<String, Object> get$response() {
        return $response;
    }

    public Map<String, Object> get$vars() {
        return $vars;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(RequestContext other) {
        return new Builder(other);
    }

    public static final class Builder {
        private Map<String, Object> $request = new HashMap<>();
        private Map<String, Object> $response = new HashMap<>();
        private Map<String, Object> $vars = new HashMap<>();

        private Builder() {}

        private Builder(RequestContext other) {
            Assert.notNull(other, "RequestContext must not be null");
            this.$request.putAll(other.get$request());
            this.$response.putAll(other.get$response());
            this.$vars.putAll(other.get$vars());
        }

        public Builder $request(Consumer<Map<String, Object>> requestConsumer) {
            Assert.notNull(requestConsumer, "requestConsumer must not be null");
            requestConsumer.accept($request);
            return this;
        }

        public Builder $request(String key, Object value) {
            this.$request.put(key, value);
            return this;
        }

        public Builder $response(Consumer<Map<String, Object>> responsetConsumer) {
            Assert.notNull(responsetConsumer, "responseConsumer must not be null");
            responsetConsumer.accept($response);
            return this;
        }

        public Builder $response(String key, Object value) {
            this.$response.put(key, value);
            return this;
        }

        public Builder $vars(Consumer<Map<String, Object>> varsConsumer) {
            Assert.notNull(varsConsumer, "varsConsumer must not be null");
            varsConsumer.accept($vars);
            return this;
        }

        public Builder $vars(String key, Object value) {
            this.$vars.put(key, value);
            return this;
        }

        public RequestContext build() {
            return new RequestContext(this);
        }
    }
}
