package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * @author renc
 */
public record ApiDatasourceCreatedEvent(DatasourceId id) implements DomainEvent<ApiDatasourceCreatedEvent> {

    @Override
    public boolean sameEventAs(ApiDatasourceCreatedEvent other) {
        return other != null && Objects.equals(this.id, other.id);
    }
}