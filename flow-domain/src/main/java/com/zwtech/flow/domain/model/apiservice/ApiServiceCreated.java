package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * @author renc
 */
public record ApiServiceCreated(ServiceId serviceId, DatasourceId datasourceId)
        implements DomainEvent<ApiServiceCreated> {

    @Override
    public boolean sameEventAs(ApiServiceCreated other) {
        return other != null &&
                Objects.equals(this.serviceId, other.serviceId) &&
                Objects.equals(this.datasourceId, other.datasourceId);
    }
}
