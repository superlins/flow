
package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.Objects;

public final class DatasourceId implements ValueObject<DatasourceId> {

    private final String value;

    public DatasourceId(String value) {
        Assert.hasText(value, "DatasourceId must not be blank");
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean sameValueAs(DatasourceId other) {
        return other != null && value.equals(other.value);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DatasourceId other && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}