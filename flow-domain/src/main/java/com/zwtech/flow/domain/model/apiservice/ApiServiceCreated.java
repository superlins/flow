package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.shared.DomainEvent;

public final class ApiServiceCreated implements DomainEvent<ApiServiceCreated> {

    private final ServiceId serviceId;
    private final DatasourceId datasourceId;

    public ApiServiceCreated(ServiceId serviceId, DatasourceId datasourceId) {
        this.serviceId = serviceId;
        this.datasourceId = datasourceId;
    }

    public ServiceId serviceId() {
        return serviceId;
    }

    public DatasourceId datasourceId() {
        return datasourceId;
    }

    @Override
    public boolean sameEventAs(ApiServiceCreated other) {
        return other != null && serviceId.sameValueAs(other.serviceId) && datasourceId.sameValueAs(other.datasourceId);
    }
}