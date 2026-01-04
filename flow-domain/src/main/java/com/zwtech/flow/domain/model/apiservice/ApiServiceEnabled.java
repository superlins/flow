package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.shared.DomainEvent;

public final class ApiServiceEnabled implements DomainEvent<ApiServiceEnabled> {

    private final ServiceId serviceId;

    public ApiServiceEnabled(ServiceId serviceId) {
        this.serviceId = serviceId;
    }

    public ServiceId serviceId() {
        return serviceId;
    }

    @Override
    public boolean sameEventAs(ApiServiceEnabled other) {
        return other != null && serviceId.sameValueAs(other.serviceId);
    }
}