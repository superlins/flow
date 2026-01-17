package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.model.workflow.WorkflowId;
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
 * - 将 ServiceContract 映射到 DatasourceContract 或 Workflow
 * - 管理完整的生命周期（创建 → 配置 → 启用 → 停用）
 *
 * 设计原则：
 * - 契约 + 映射规则 + (Datasource 引用 OR Workflow 引用) = ApiService
 * - 通过 ServiceMapping 的 mode 属性区分：DATASOURCE 模式或 WORKFLOW 模式
 *
 * @author renc
 */
public final class ApiService implements DomainEntity<ApiService> {

    private final ServiceId id;
    private ServiceStatus status;

    private ServiceContract contract;
    private ServiceMapping mapping; // 输入输出映射规则 + 源引用（Datasource 或 Workflow）

    private String name;
    private String description;

    private Instant createdAt;
    private Instant updatedAt;

    private final List<Object> domainEvents = new ArrayList<>();

    private ApiService(ServiceId id, ServiceContract contract, ServiceMapping mapping) {
        Assert.notNull(id, "ServiceId must not be null");
        Assert.notNull(contract, "ServiceContract must not be null");
        Assert.notNull(mapping, "mapping must not be null");

        this.id = id;
        this.contract = contract;
        this.mapping = mapping;
        this.status = ServiceStatus.DISABLED;

        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * 创建 Datasource 模式的 ApiService
     *
     * @param id 服务标识
     * @param name 服务名称
     * @param description 服务描述
     * @param datasourceId 引用的 Datasource 标识
     * @param datasourceVersion Datasource 版本
     * @param contract 服务契约
     * @param inputMapping 输入映射规则
     * @param outputMapping 输出映射规则
     * @return 新创建的 ApiService（状态为 DISABLED）
     */
    public static ApiService create(
            ServiceId id,
            String name,
            String description,
            DatasourceId datasourceId,
            ServiceContract contract,
            ServiceMapping inputMapping,
            ServiceMapping outputMapping) {

        Assert.notNull(id, "ServiceId must not be null");
        Assert.hasText(name, "name must not be empty");
        Assert.notNull(datasourceId, "DatasourceId must not be null");
        Assert.notNull(contract, "ServiceContract must not be null");

        ServiceMapping mapping = ServiceMapping.datasource(
                datasourceId,
                datasourceId.version(),
                inputMapping.inputMapping(),
                outputMapping.outputMapping()
        );

        ApiService service = new ApiService(id, contract, mapping);
        service.name = name;
        service.description = description != null ? description : "";
        service.domainEvents.add(new ApiServiceCreated(id, datasourceId));
        return service;
    }

    /**
     * 创建 Workflow 模式的 ApiService
     *
     * @param id 服务标识
     * @param name 服务名称
     * @param description 服务描述
     * @param workflowId 引用的 Workflow 标识
     * @param workflowVersion Workflow 版本
     * @param contract 服务契约
     * @param inputMapping 输入映射规则
     * @param outputMapping 输出映射规则
     * @return 新创建的 ApiService（状态为 DISABLED）
     */
    public static ApiService createWorkflow(
            ServiceId id,
            String name,
            String description,
            WorkflowId workflowId,
            ServiceContract contract,
            ServiceMapping inputMapping,
            ServiceMapping outputMapping) {

        Assert.notNull(id, "ServiceId must not be null");
        Assert.hasText(name, "name must not be empty");
        Assert.notNull(workflowId, "WorkflowId must not be null");
        Assert.notNull(contract, "ServiceContract must not be null");

        ServiceMapping mapping = ServiceMapping.workflow(
                workflowId.key(),
                workflowId.version(),
                inputMapping.inputMapping(),
                outputMapping.outputMapping()
        );

        ApiService service = new ApiService(id, contract, mapping);
        service.name = name;
        service.description = description != null ? description : "";
        service.domainEvents.add(new ApiServiceCreated(id, null)); // datasourceId 为 null 表示 workflow 模式
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
            ServiceMapping mapping,
            Instant createdAt,
            Instant updatedAt) {

        Assert.notNull(id, "ServiceId must not be null");
        Assert.hasText(name, "name must not be empty");
        Assert.notNull(status, "status must not be null");
        Assert.notNull(contract, "ServiceContract must not be null");
        Assert.notNull(mapping, "mapping must not be null");

        ApiService service = new ApiService(id, contract, mapping);
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

        // 验证映射规则已正确配置
        if (mapping.mode() == ServiceMapping.ServiceMode.DATASOURCE && mapping.datasourceId() == null) {
            throw new IllegalStateException("Datasource mode requires datasourceId to be configured");
        }
        if (mapping.mode() == ServiceMapping.ServiceMode.WORKFLOW && mapping.workflowId() == null) {
            throw new IllegalStateException("Workflow mode requires workflowId to be configured");
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
     * 更新服务契约
     *
     * @param newContract 新契约
     */
    public void updateContract(ServiceContract newContract) {
        Assert.notNull(newContract, "newContract must not be null");
        this.contract = newContract;
        touch();
    }

    /**
     * 更新映射规则
     *
     * @param newMapping 新映射规则
     */
    public void updateMapping(ServiceMapping newMapping) {
        Assert.notNull(newMapping, "newMapping must not be null");
        this.mapping = newMapping;
        touch();
    }

    /**
     * 切换到 Datasource 模式
     */
    public void switchToDatasource(DatasourceId datasourceId,
                                   ServiceMapping inputMapping,
                                   ServiceMapping outputMapping) {
        Assert.notNull(datasourceId, "DatasourceId must not be null");
        if (this.status != ServiceStatus.DISABLED) {
            throw new IllegalStateException("Can only switch mode when service is DISABLED");
        }

        this.mapping = ServiceMapping.datasource(
                datasourceId,
                datasourceId.version(),
                inputMapping.inputMapping(),
                outputMapping.outputMapping()
        );
        touch();
    }

    /**
     * 切换到 Workflow 模式
     */
    public void switchToWorkflow(WorkflowId workflowId,
                                  ServiceMapping inputMapping,
                                  ServiceMapping outputMapping) {
        Assert.notNull(workflowId, "WorkflowId must not be null");
        if (this.status != ServiceStatus.DISABLED) {
            throw new IllegalStateException("Can only switch mode when service is DISABLED");
        }

        this.mapping = ServiceMapping.workflow(
                workflowId.key(),
                workflowId.version(),
                inputMapping.inputMapping(),
                outputMapping.outputMapping()
        );
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    /* ========= Getters ========= */

    /**
     * 是否为 Datasource 模式
     */
    public boolean isDatasourceMode() {
        return mapping.mode() == ServiceMapping.ServiceMode.DATASOURCE;
    }

    /**
     * 是否为 Workflow 模式
     */
    public boolean isWorkflowMode() {
        return mapping.mode() == ServiceMapping.ServiceMode.WORKFLOW;
    }

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

    /**
     * 获取 Datasource 引用（仅 Datasource 模式有效）
     */
    public DatasourceId datasourceId() {
        return mapping.datasourceId();
    }

    /**
     * 获取 Workflow 引用（仅 Workflow 模式有效）
     */
    public String workflowId() {
        return mapping.workflowId();
    }

    public ServiceMapping mapping() {
        return mapping;
    }

    /**
     * 获取 Workflow 完整标识（仅 Workflow 模式有效）
     */
    public WorkflowId fullWorkflowId() {
        if (!isWorkflowMode()) {
            return null;
        }
        return WorkflowId.of(mapping.workflowId(), mapping.workflowVersion());
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
