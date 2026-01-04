package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.ValueObject;

import java.util.Objects;

/**
 * @author renc
 */
public class Extension implements ValueObject<Extension> {
    private final String id;

    public Extension(String id) {
        this.id = id;
    }

    @Override
    public boolean sameValueAs(Extension other) {
        return other != null && id.equals(other.id);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Extension other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
