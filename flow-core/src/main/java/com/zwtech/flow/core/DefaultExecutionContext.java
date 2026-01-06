package com.zwtech.flow.core;

import tools.jackson.databind.JsonNode;

import java.util.Optional;
import java.util.Objects;

/**
 * ExecutionContext 的不可变实现。
 *
 * @author renc
 */
public final class DefaultExecutionContext implements ExecutionContext {

    private final Optional<JsonNode> input;
    private final Optional<JsonNode> output;
    private final Optional<DerivedContext> derived;

    public DefaultExecutionContext(Optional<JsonNode> input,
                                   Optional<JsonNode> output,
                                   Optional<DerivedContext> derived) {
        this.input = input != null ? input : Optional.empty();
        this.output = output != null ? output : Optional.empty();
        this.derived = derived != null ? derived : Optional.empty();
    }

    public static DefaultExecutionContext empty() {
        return new DefaultExecutionContext(Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Override
    public Optional<JsonNode> input() {
        return input;
    }

    @Override
    public Optional<JsonNode> output() {
        return output;
    }

    @Override
    public Optional<DerivedContext> derived() {
        return derived;
    }

    public DefaultExecutionContext withInput(JsonNode in) {
        return new DefaultExecutionContext(Optional.ofNullable(in), output, derived);
    }

    public DefaultExecutionContext withOutput(JsonNode out) {
        return new DefaultExecutionContext(input, Optional.ofNullable(out), derived);
    }

    public DefaultExecutionContext withDerived(DerivedContext ctx) {
        return new DefaultExecutionContext(input, output, Optional.ofNullable(ctx));
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DefaultExecutionContext other)
                && Objects.equals(input, other.input)
                && Objects.equals(output, other.output)
                && Objects.equals(derived, other.derived);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, output, derived);
    }

    @Override
    public String toString() {
        return "DefaultExecutionContext{input=" + input + ", output=" + output + ", derived=" + derived + '}';
    }
}

