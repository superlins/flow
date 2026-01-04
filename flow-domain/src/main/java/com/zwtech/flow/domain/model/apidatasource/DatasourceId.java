package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.Objects;

public final class DatasourceId implements ValueObject<DatasourceId> {

    private final DatasourceKey key;
    private final DatasourceVersion version;

    public DatasourceId(DatasourceKey key, DatasourceVersion version) {
        Assert.notNull(key, "DatasourceKey must not be null");
        Assert.notNull(version, "DatasourceVersion must not be null");
        this.key = key;
        this.version = version;
    }

    public DatasourceKey key() {
        return key;
    }

    public DatasourceVersion version() {
        return version;
    }

    @Override
    public boolean sameValueAs(DatasourceId other) {
        return other != null
            && key.sameValueAs(other.key)
            && version.sameValueAs(other.version);
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