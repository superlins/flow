package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.ValueObject;

import java.util.Map;

/**
 * @author renc
 */
public class Option implements ValueObject<Option> {

    private final Map<String, Object> options;

    public Option(Map<String, Object> options) {
        this.options = options;
    }

    @Override
    public boolean sameValueAs(Option other) {
        return other != null && options.equals(other.options);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Option other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return options.hashCode();
    }
}
