package com.zwtech.flow.domain.model.apidatasource;

import org.springframework.util.Assert;

import java.util.Objects;

public final class DatasourceVersion {

    private final String value;

    public DatasourceVersion(String value) {
        Assert.hasText(value, "datasourceVersion must not be blank");
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DatasourceVersion other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}