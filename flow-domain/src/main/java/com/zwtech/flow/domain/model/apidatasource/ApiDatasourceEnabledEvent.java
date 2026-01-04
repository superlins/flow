package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * @author renc
 */
public record ApiDatasourceEnabledEvent(DatasourceId id) implements DomainEvent<ApiDatasourceEnabledEvent> {

    @Override
    public boolean sameEventAs(ApiDatasourceEnabledEvent other) {
        return other != null && Objects.equals(this.id, other.id);
    }
}
