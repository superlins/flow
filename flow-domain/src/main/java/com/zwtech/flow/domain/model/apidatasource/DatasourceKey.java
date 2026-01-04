package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.Objects;

public final class DatasourceKey implements ValueObject<DatasourceKey> {

    private final String value;

    public DatasourceKey(String value) {
        Assert.hasText(value, "DatasourceKey must not be empty");
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean sameValueAs(DatasourceKey other) {
        return other != null && Objects.equals(this.value, other.value);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DatasourceKey other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}