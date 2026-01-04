package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.shared.DomainEntity;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 契约 + 绑定规则 + 一个 Datasource 引用 = ApiService，BindingSpec = 契约之间的数据流声明
 *
 * @author renc
 */
public final class ApiService implements DomainEntity<ApiService> {

    private final ServiceId id;
    private ServiceStatus status;

    private ServiceContract contract;
    private DatasourceId datasourceId;
    private BindingSpec bindingSpec;

    private String name;
    private String description;

    private final Instant createdAt;
    private Instant updatedAt;

    private final List<Object> domainEvents = new ArrayList<>();

    private ApiService(ServiceId id,
            ServiceContract contract,
            DatasourceId datasourceId) {

        Assert.notNull(id, "ServiceId must not be null");
        Assert.notNull(contract, "ServiceContract must not be null");
        Assert.notNull(datasourceId, "DatasourceId must not be null");

        this.id = id;
        this.contract = contract;
        this.datasourceId = datasourceId;
        this.status = ServiceStatus.DISABLED;

        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static ApiService create(
            ServiceId id,
            DatasourceId datasourceId,
            ServiceContract contract
    ) {
        Assert.notNull(id, "ServiceId must not be null");
        Assert.notNull(datasourceId, "DatasourceId must not be null");
        Assert.notNull(contract, "ServiceContract must not be null");

        ApiService ds = new ApiService(id, contract, datasourceId);
        ds.domainEvents.add(new ApiServiceCreated(id, datasourceId));
        return ds;
    }

    public void enable() {
        if (this.status == ServiceStatus.ENABLED) {
            return;
        }
        this.status = ServiceStatus.ENABLED;
        this.domainEvents.add(new ApiServiceEnabled(this.id));
        touch();
    }

    public void disable() {
        if (this.status == ServiceStatus.DISABLED) {
            return;
        }
        this.status = ServiceStatus.DISABLED;
        domainEvents.add(new ApiServiceDisabled(id));
        touch();
    }

    public boolean isEnabled() {
        return status == ServiceStatus.ENABLED;
    }

    public void updateContract(ServiceContract newContract,
            BindingSpec newBindingSpec) {
        Assert.notNull(newContract, "newContract must not be null");
        this.contract = newContract;
        this.bindingSpec = newBindingSpec;
        touch();
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean sameIdentityAs(ApiService other) {
        return other != null && this.id.sameValueAs(other.id);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ApiService that && sameIdentityAs(that));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
