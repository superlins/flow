package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.JsonSchema;
import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

public final class DatasourceContract implements ValueObject<DatasourceContract> {

    private final JsonSchema inputSchema;
    private final JsonSchema outputSchema;

    public DatasourceContract(JsonSchema inputSchema,
                              JsonSchema outputSchema) {
        Assert.notNull(inputSchema, "inputSchema must not be null");
        Assert.notNull(outputSchema, "outputSchema must not be null");
        this.inputSchema = inputSchema;
        this.outputSchema = outputSchema;
    }

    public JsonSchema inputSchema() {
        return inputSchema;
    }

    public JsonSchema outputSchema() {
        return outputSchema;
    }

    @Override
    public boolean sameValueAs(DatasourceContract other) {
        return other != null && inputSchema.equals(other.inputSchema) && outputSchema.equals(other.outputSchema);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        DatasourceContract that = (DatasourceContract) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        int result = inputSchema.hashCode();
        result = 31 * result + outputSchema.hashCode();
        return result;
    }

}