package com.zwtech.flow.core;

import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * @author renc
 */
public interface ExecutionExchange {

    ExecutionContext context();

    ExecutionExchange mutate(UnaryOperator<ExecutionContext> operator);

    <T> Optional<T> getAttribute(ExchangeAttributeKey<T> key);

    ExecutionExchange withAttribute(ExchangeAttributeKey<?> key, Object value);
}
