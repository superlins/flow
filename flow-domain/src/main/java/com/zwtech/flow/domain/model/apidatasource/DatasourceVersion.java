package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.Objects;

public final class DatasourceVersion implements ValueObject<DatasourceVersion> {

    private final int value;

    public DatasourceVersion(int value) {
        Assert.isTrue(value > 0, "DatasourceVersion must be greater than 0");
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public boolean sameValueAs(DatasourceVersion other) {
        return other != null && this.value == other.value;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DatasourceVersion other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}