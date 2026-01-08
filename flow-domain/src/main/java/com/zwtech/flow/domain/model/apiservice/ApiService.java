package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.shared.DomainEntity;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ApiService 聚合根
 * 
 * 核心职责：
 * - 表达一个面向业务/产品的 API 服务
 * - 将 ServiceContract 映射到 DatasourceContract
 * - 管理完整的生命周期（创建 → 配置 → 启用 → 停用）
 * 
 * 设计原则：
 * - 契约 + 映射规则 + 一个 Datasource 引用 = ApiService
 *
 * @author renc
 */
public final class ApiService implements DomainEntity<ApiService> {

    private final ServiceId id;
    private ServiceStatus status;

    private ServiceContract contract;
    private DatasourceId datasourceId;
    private String operationKey; // Rule 3: ApiService 必须显式绑定 operationKey

    private String name;
    private String description;

    private Instant createdAt;
    private Instant updatedAt;

    private final List<Object> domainEvents = new ArrayList<>();

    private ApiService(ServiceId id,
            ServiceContract contract,
            DatasourceId datasourceId,
            String operationKey) {

        Assert.notNull(id, "ServiceId must not be null");
        Assert.notNull(contract, "ServiceContract must not be null");
        Assert.notNull(datasourceId, "DatasourceId must not be null");
        Assert.hasText(operationKey, "operationKey must not be empty");

        this.id = id;
        this.contract = contract;
        this.datasourceId = datasourceId;
        this.operationKey = operationKey;
        this.status = ServiceStatus.DISABLED;

        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * 创建新的 ApiService
     * 
     * @param id 服务标识
     * @param name 服务名称
     * @param description 服务描述
     * @param datasourceId 引用的 Datasource 标识
     * @param contract 服务契约
     * @param bindingSpec 绑定规则
     * @return 新创建的 ApiService（状态为 DISABLED）
     */
    public static ApiService create(
            ServiceId id,
            String name,
            String description,
            DatasourceId datasourceId,
            String operationKey,
            ServiceContract contract) {
        
        Assert.notNull(id, "ServiceId must not be null");
        Assert.hasText(name, "name must not be empty");
        Assert.notNull(datasourceId, "DatasourceId must not be null");
        Assert.hasText(operationKey, "operationKey must not be empty");
        Assert.notNull(contract, "ServiceContract must not be null");

        ApiService service = new ApiService(id, contract, datasourceId, operationKey);
        service.name = name;
        service.description = description != null ? description : "";
        service.domainEvents.add(new ApiServiceCreated(id, datasourceId));
        return service;
    }

    /**
     * 从持久化实体恢复领域对象
     * Repository 实现使用此方法重建领域对象
     */
    public static ApiService restore(
            ServiceId id,
            String name,
            String description,
            ServiceStatus status,
            ServiceContract contract,
            DatasourceId datasourceId,
            String operationKey,
            Instant createdAt,
            Instant updatedAt) {
        
        Assert.notNull(id, "ServiceId must not be null");
        Assert.hasText(name, "name must not be empty");
        Assert.notNull(status, "status must not be null");
        Assert.notNull(contract, "ServiceContract must not be null");
        Assert.notNull(datasourceId, "DatasourceId must not be null");
        Assert.hasText(operationKey, "operationKey must not be empty");

        ApiService service = new ApiService(id, contract, datasourceId, operationKey);
        service.name = name;
        service.description = description != null ? description : "";
        service.status = status;
        service.createdAt = createdAt != null ? createdAt : Instant.now();
        service.updatedAt = updatedAt != null ? updatedAt : Instant.now();
        // 注意：恢复时不发布领域事件
        return service;
    }

    /* ========= Domain Behavior ========= */

    /**
     * 启用服务
     * 只有 Enabled 状态的服务才允许被调用
     */
    public void enable() {
        if (this.status == ServiceStatus.ENABLED) {
            return;
        }
        this.status = ServiceStatus.ENABLED;
        this.domainEvents.add(new ApiServiceEnabled(this.id));
        touch();
    }

    /**
     * 停用服务
     */
    public void disable() {
        if (this.status == ServiceStatus.DISABLED) {
            return;
        }
        this.status = ServiceStatus.DISABLED;
        domainEvents.add(new ApiServiceDisabled(id));
        touch();
    }

    /**
     * 更新服务元数据（名称、描述）
     * 
     * @param name 新名称
     * @param description 新描述
     */
    public void updateMetadata(String name, String description) {
        Assert.hasText(name, "name must not be empty");
        this.name = name;
        this.description = description != null ? description : "";
        touch();
    }

    /**
     * 更新服务契约和绑定规则
     * 
     * @param newContract 新契约
     */
    public void updateContract(ServiceContract newContract) {
        Assert.notNull(newContract, "newContract must not be null");
        this.contract = newContract;
        touch();
    }

    /**
     * 更新引用的 Datasource
     * 
     * @param newDatasourceId 新的 Datasource 标识
     */
    public void updateDatasource(DatasourceId newDatasourceId) {
        Assert.notNull(newDatasourceId, "newDatasourceId must not be null");
        this.datasourceId = newDatasourceId;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    /* ========= Getters ========= */

    public boolean isEnabled() {
        return status == ServiceStatus.ENABLED;
    }

    public ServiceId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public ServiceStatus status() {
        return status;
    }

    public ServiceContract contract() {
        return contract;
    }

    public DatasourceId datasourceId() {
        return datasourceId;
    }

    public String operationKey() {
        return operationKey;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
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
