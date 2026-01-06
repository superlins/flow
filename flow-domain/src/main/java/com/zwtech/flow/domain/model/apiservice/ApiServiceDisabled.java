package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.shared.DomainEvent;

/**
 * @author renc
 */
public final class ApiServiceDisabled implements DomainEvent<ApiServiceDisabled> {

    private final ServiceId serviceId;

    public ApiServiceDisabled(ServiceId serviceId) {
        this.serviceId = serviceId;
    }

    public ServiceId serviceId() {
        return serviceId;
    }

    @Override
    public boolean sameEventAs(ApiServiceDisabled other) {
        return other != null && serviceId.sameValueAs(other.serviceId);
    }
}