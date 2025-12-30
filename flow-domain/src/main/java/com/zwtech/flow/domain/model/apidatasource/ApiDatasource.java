package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.DomainEntity;
import org.springframework.util.Assert;

import java.time.Instant;

public final class ApiDatasource implements DomainEntity<ApiDatasource> {

    private final DatasourceId datasourceId;
    private final DatasourceVersion version;
    private DatasourceStatus status;

    private final DatasourceContract contract;
    private final DatasourceEndpoint endpoint;

    private final String name;
    private final String description;

    private final Instant createdAt;
    private Instant updatedAt;

    public ApiDatasource(
            DatasourceId datasourceId,
            DatasourceVersion version,
            DatasourceContract contract,
            DatasourceEndpoint endpoint,
            String name,
            String description
    ) {
        Assert.notNull(datasourceId, "datasourceId must not be null");
        Assert.notNull(version, "version must not be null");
        Assert.notNull(contract, "contract must not be null");
        Assert.notNull(endpoint, "endpoint must not be null");

        this.datasourceId = datasourceId;
        this.version = version;
        this.contract = contract;
        this.endpoint = endpoint;

        this.name = name;
        this.description = description;

        this.status = DatasourceStatus.ENABLED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /* ========= Domain Behavior ========= */

    public void disable() {
        this.status = DatasourceStatus.DISABLED;
        touch();
    }

    public void enable() {
        this.status = DatasourceStatus.ENABLED;
        touch();
    }

    /**
     * 核心领域能力：
     * 提供契约给 ApiService 校验使用
     */
    public DatasourceContract exposeContract() {
        return contract;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    /* ========= Getters ========= */

    public DatasourceId datasourceId() {
        return datasourceId;
    }

    public DatasourceVersion version() {
        return version;
    }

    public DatasourceStatus status() {
        return status;
    }

    public DatasourceEndpoint endpoint() {
        return endpoint;
    }

    @Override
    public boolean sameIdentityAs(ApiDatasource other) {
        return other != null && datasourceId.sameValueAs(other.datasourceId);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ApiDatasource other) && sameIdentityAs(other);
    }

    @Override
    public int hashCode() {
        return datasourceId.hashCode();
    }
}