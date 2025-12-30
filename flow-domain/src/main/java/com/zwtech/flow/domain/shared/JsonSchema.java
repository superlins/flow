package com.zwtech.flow.domain.shared;

import org.springframework.util.Assert;
import tools.jackson.databind.JsonNode;

/**
 * @author renc
 */
public final class JsonSchema implements ValueObject<JsonSchema> {

    private final JsonNode schema;

    public JsonSchema(JsonNode schema) {
        Assert.notNull(schema, "JsonSchema must not be null");
        this.schema = schema;
    }

    public JsonNode schema() {
        return schema;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        JsonSchema that = (JsonSchema) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return schema.hashCode();
    }

    @Override
    public boolean sameValueAs(JsonSchema other) {
        return other != null && schema.equals(other.schema);
    }
}
