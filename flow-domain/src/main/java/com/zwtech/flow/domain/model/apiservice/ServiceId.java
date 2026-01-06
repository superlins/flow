package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @author renc
 */
public final class ServiceId implements ValueObject<ServiceId> {

    private final String value;

    public ServiceId(String value) {
        Assert.hasText(value, "ApiServiceId must not be blank");
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean sameValueAs(ServiceId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ServiceId that && sameValueAs(that));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "ServiceId{value='" + value + "'}";
    }
}