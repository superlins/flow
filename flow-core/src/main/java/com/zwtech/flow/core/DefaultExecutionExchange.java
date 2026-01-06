package com.zwtech.flow.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

/**
 * ExecutionExchange 的不可变实现。
 *
 * @author renc
 */
public final class DefaultExecutionExchange implements ExecutionExchange {

    private final ExecutionContext context;
    private final Map<ExchangeAttributeKey<?>, Object> attributes;

    public DefaultExecutionExchange(ExecutionContext context, Map<ExchangeAttributeKey<?>, Object> attributes) {
        this.context = context == null ? DefaultExecutionContext.empty() : context;
        this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static DefaultExecutionExchange empty() {
        return new DefaultExecutionExchange(DefaultExecutionContext.empty(), Map.of());
    }

    @Override
    public ExecutionContext context() {
        return context;
    }

    @Override
    public ExecutionExchange mutate(UnaryOperator<ExecutionContext> operator) {
        ExecutionContext newCtx = operator.apply(context);
        return new DefaultExecutionExchange(newCtx, attributes);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getAttribute(ExchangeAttributeKey<T> key) {
        return Optional.ofNullable((T) attributes.get(key));
    }

    @Override
    public ExecutionExchange withAttribute(ExchangeAttributeKey<?> key, Object value) {
        Map<ExchangeAttributeKey<?>, Object> copy = new ConcurrentHashMap<>(attributes);
        copy.put(key, value);
        return new DefaultExecutionExchange(context, copy);
    }

    @Override
    public String toString() {
        return "DefaultExecutionExchange{context=" + context + ", attributes=" + attributes + '}';
    }
}

