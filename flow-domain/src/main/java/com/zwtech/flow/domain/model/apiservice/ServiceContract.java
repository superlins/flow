package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @author renc
 */
public final class ServiceContract implements ValueObject<ServiceContract> {
    private final String inputSchema;
    private final String outputSchema;

    public ServiceContract(String inputSchema, String outputSchema) {
        Assert.hasText(inputSchema, "ApiService inputSchema must not be blank");
        Assert.hasText(outputSchema, "ApiService outputSchema must not be blank");
        this.inputSchema = inputSchema;
        this.outputSchema = outputSchema;
    }

    public String inputSchema() {
        return inputSchema;
    }

    public String outputSchema() {
        return outputSchema;
    }

    @Override
    public boolean sameValueAs(ServiceContract other) {
        return other != null
               && inputSchema.equals(other.inputSchema)
               && outputSchema.equals(other.outputSchema);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ServiceContract that && sameValueAs(that));
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputSchema, outputSchema);
    }

    @Override
    public String toString() {
        return "ServiceContract{}";
    }
}
