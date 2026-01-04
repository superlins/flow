package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.Objects;

public final class DatasourceId implements ValueObject<DatasourceId> {

    private final String key;
    private final int version;

    public DatasourceId(String key, int version) {
        Assert.hasText(key, "Datasource key must not be empty");
        Assert.isTrue(version > 0, "Datasource version must be positive");
        this.key = key;
        this.version = version;
    }

    public String key() {
        return key;
    }

    public int version() {
        return version;
    }

    @Override
    public boolean sameValueAs(DatasourceId other) {
        return other != null
               && Objects.equals(this.key, other.key)
               && this.version == other.version;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DatasourceId other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, version);
    }
}