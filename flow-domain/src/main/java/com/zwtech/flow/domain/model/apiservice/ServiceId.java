package com.zwtech.flow.domain.model.apiservice;

import org.springframework.util.Assert;

/**
 * @author renc
 */
public final class ServiceId {

    private final String value;

    public ServiceId(String value) {
        Assert.hasText(value, "serviceId must not be blank");
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ServiceId serviceId = (ServiceId) o;
        return value.equals(serviceId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "ServiceId{" +
                "value='" + value + '\'' +
                '}';
    }
}
