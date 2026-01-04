package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

public final class DatasourceContract implements ValueObject<DatasourceContract> {

    private final String inputSchema;
    private final String outputSchema;
    private final boolean strict;

    public DatasourceContract(String inputSchema, String outputSchema, boolean strict) {
        Assert.hasText(inputSchema, "InputSchema must not be empty");
        Assert.hasText(outputSchema, "OutputSchema must not be empty");
        this.inputSchema = inputSchema;
        this.outputSchema = outputSchema;
        this.strict = strict;
    }

    public String inputSchema() {
        return inputSchema;
    }

    public String outputSchema() {
        return outputSchema;
    }

    public boolean strict() {
        return strict;
    }

    @Override
    public boolean sameValueAs(DatasourceContract other) {
        return other != null && strict == other.strict && inputSchema.equals(other.inputSchema) && outputSchema.equals(other.outputSchema);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;

        DatasourceContract that = (DatasourceContract) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        int result = inputSchema.hashCode();
        result = 31 * result + outputSchema.hashCode();
        result = 31 * result + Boolean.hashCode(strict);
        return result;
    }
}