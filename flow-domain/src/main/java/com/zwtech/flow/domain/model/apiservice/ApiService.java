package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.model.apidatasource.DatasourceContract;
import org.springframework.util.Assert;

import java.time.Instant;

/**
 * @author renc
 */
public final class ApiService {

    private final ServiceId serviceId;
    private ServiceStatus status;

    private ServiceContract contract;
    private DatasourceRef datasourceRef;
    private BindingSpec bindingSpec;

    private final String name;
    private String description;

    private final Instant createdAt;
    private Instant updatedAt;

    public ApiService(
            ServiceId serviceId,
            ServiceContract contract,
            DatasourceRef datasourceRef,
            BindingSpec bindingSpec,
            String name,
            String description
    ) {
        Assert.notNull(serviceId, "serviceId must not be null");
        Assert.notNull(contract, "contract must not be null");
        Assert.notNull(datasourceRef, "datasourceRef must not be null");
        Assert.notNull(bindingSpec, "bindingSpec must not be null");

        this.serviceId = serviceId;
        this.contract = contract;
        this.datasourceRef = datasourceRef;
        this.bindingSpec = bindingSpec;

        this.name = name;
        this.description = description;

        this.status = ServiceStatus.ENABLED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void validateAgainstDatasource(DatasourceContract datasourceContract) {
        Assert.notNull(datasourceContract, "datasourceContract must not be null");

        contract.assertCompatibleWith(
                datasourceContract,
                bindingSpec
        );
    }

    public void disable() {
        this.status = ServiceStatus.DISABLED;
        touch();
    }

    public void enable() {
        this.status = ServiceStatus.ENABLED;
        touch();
    }

    public void updateContract(ServiceContract newContract) {
        Assert.notNull(newContract, "newContract must not be null");
        this.contract = newContract;
        touch();
    }

    public void updateBindingSpec(BindingSpec newSpec) {
        Assert.notNull(newSpec, "bindingSpec must not be null");
        this.bindingSpec = newSpec;
        touch();
    }

    public void updateDescription(String newDescription) {
        Assert.notNull(newDescription, "newDescription must not be null");
        this.description = newDescription;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public ServiceId serviceId() {
        return serviceId;
    }

    public ServiceStatus status() {
        return status;
    }

    public ServiceContract contract() {
        return contract;
    }

    public DatasourceRef datasourceRef() {
        return datasourceRef;
    }

    public BindingSpec bindingSpec() {
        return bindingSpec;
    }
}
