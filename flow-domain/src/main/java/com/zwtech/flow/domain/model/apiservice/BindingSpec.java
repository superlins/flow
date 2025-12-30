package com.zwtech.flow.domain.model.apiservice;

import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author renc
 */
public final class BindingSpec {
    /**
     * targetField -> expression
     * e.g. user_id -> {{ $.uid }}
     */
    private final Map<String, String> input;

    /**
     * datasourceField -> expression
     */
    private final Map<String, String> output;

    public BindingSpec(Map<String, String> input,
                       Map<String, String> output) {

        this.input = input == null ? Map.of() : Map.copyOf(input);
        this.output = output == null ? Map.of() : Map.copyOf(output);

        this.input.forEach((k, v) ->
                Assert.hasText(v,
                        "binding expression must not be blank, target=" + k)
        );
    }

    public Map<String, String> input() {
        return input;
    }

    public Map<String, String> output() {
        return output;
    }

    public boolean isEmpty() {
        return input.isEmpty() && output.isEmpty();
    }

}
