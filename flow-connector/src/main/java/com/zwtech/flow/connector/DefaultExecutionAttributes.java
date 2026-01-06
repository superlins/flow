package com.zwtech.flow.connector;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ExecutionAttributes 的不可变实现。
 *
 * @author renc
 */
public final class DefaultExecutionAttributes implements ExecutionAttributes {

    private final Map<String, Object> attrs;

    public DefaultExecutionAttributes(Map<String, Object> attrs) {
        this.attrs = attrs == null ? Map.of() : Map.copyOf(attrs);
    }

    public static DefaultExecutionAttributes empty() {
        return new DefaultExecutionAttributes(Map.of());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> get(String key) {
        return Optional.ofNullable((T) attrs.get(key));
    }

    @Override
    public ExecutionAttributes with(String key, Object value) {
        Map<String, Object> copy = new ConcurrentHashMap<>(attrs);
        copy.put(key, value);
        return new DefaultExecutionAttributes(copy);
    }

    @Override
    public String toString() {
        return "DefaultExecutionAttributes" + attrs;
    }
}

