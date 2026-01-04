package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.model.apidatasource.connection.ConnectionSpec;
import com.zwtech.flow.domain.model.apidatasource.operation.OperationSpec;
import com.zwtech.flow.domain.shared.DomainEntity;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class ApiDatasource implements DomainEntity<ApiDatasource> {

    private final DatasourceId id;

    private String name;
    private String description;

    private DatasourceType type;
    private DatasourceStatus status;

    private DatasourceContract contract;
    private OperationSpec operation;
    private ConnectionSpec connection;

    private List<Extension> extension;

    private final List<Object> domainEvents = new ArrayList<>();

    private final Instant createdAt;
    private Instant updatedAt;

    private ApiDatasource(DatasourceId id) {
        this.id = id;
        this.status = DatasourceStatus.DISABLED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static ApiDatasource create(DatasourceId id) {
        Assert.notNull(id, "DatasourceId must not be null");
        ApiDatasource ds = new ApiDatasource(id);
        ds.domainEvents.add(new ApiDatasourceCreatedEvent(id));
        return ds;
    }

    /* ========= Domain Behavior ========= */

    public void enable() {
        if (this.status == DatasourceStatus.ENABLED) {
            return;
        }
        this.status = DatasourceStatus.ENABLED;
        domainEvents.add(new ApiDatasourceEnabledEvent(id));
        touch();
    }

    public void disable() {
        if (this.status == DatasourceStatus.DISABLED) {
            return;
        }
        this.status = DatasourceStatus.DISABLED;
        domainEvents.add(new ApiDatasourceDisabledEvent(id));
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    /* ========= Getters ========= */
    public boolean isEnabled() {
        return status == DatasourceStatus.ENABLED;
    }

    public DatasourceId id() {
        return id;
    }

    public DatasourceType type() {
        return type;
    }

    public DatasourceContract contract() {
        return contract;
    }

    public OperationSpec operation() {
        return operation;
    }

    public ConnectionSpec connection() {
        return connection;
    }

    public DatasourceStatus status() {
        return status;
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    @Override
    public boolean sameIdentityAs(ApiDatasource other) {
        return other != null && this.id.sameValueAs(other.id);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ApiDatasource other) && sameIdentityAs(other);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}