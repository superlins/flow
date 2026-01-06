package com.zwtech.flow.core;

import java.util.Objects;

/**
 * 不可变的属性键，用于在 ExecutionExchange / ExecutionAttributes 中存取属性。
 *
 * @author renc
 */
public final class ExchangeAttributeKey<T> {

    private final String name;

    public ExchangeAttributeKey(String name) {
        this.name = Objects.requireNonNull(name, "name must not be null");
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ExchangeAttributeKey<?> other) && Objects.equals(this.name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ExchangeAttributeKey{name='" + name + "'}";
    }
}
