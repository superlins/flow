package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * @author renc
 */
public record ApiDatasourceDisabledEvent(DatasourceId id) implements DomainEvent<ApiDatasourceDisabledEvent> {

    @Override
    public boolean sameEventAs(ApiDatasourceDisabledEvent other) {
        return other != null && Objects.equals(this.id, other.id);
    }
}
