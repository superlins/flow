package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.model.apidatasource.operation.DatasourceOperation;
import com.zwtech.flow.domain.model.apidatasource.connection.DatasourceConnection;
import com.zwtech.flow.domain.shared.DomainEntity;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * ApiDatasource 聚合根
 * 
 * 核心职责：
 * - 表达一个可被 ApiService 引用、可执行、具备明确契约承诺的底层数据源能力
 * - 管理完整的生命周期（创建 → 配置 → 启用 → 停用）
 * - 保证业务不变量（DS-1: 被引用不可修改核心字段）
 *
 * @author renc
 */
public final class ApiDatasource implements DomainEntity<ApiDatasource> {

    private final DatasourceId id;

    private String name;
    private String description;

    private DatasourceType type;
    private DatasourceStatus status;
    private DatasourceConnection connection;
    private DatasourceContract contract;
    private DatasourceOperation operation;

    private List<Extension> extensions;

    private final List<Object> domainEvents = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;

    private ApiDatasource(DatasourceId id) {
        this.id = id;
        this.status = DatasourceStatus.DISABLED;
        this.extensions = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * 从持久化实体恢复领域对象
     * Repository 实现使用此方法重建领域对象
     * 
     * 注意：恢复时不发布领域事件，因为这是从数据库重建，不是新的业务操作
     */
    public static ApiDatasource restore(
            DatasourceId id,
            String name,
            String description,
            DatasourceType type,
            DatasourceStatus status,
            DatasourceOperation operation,
            DatasourceConnection connection,
            List<Extension> extensions,
            Instant createdAt,
            Instant updatedAt) {
        
        Assert.notNull(id, "DatasourceId must not be null");
        Assert.hasText(name, "name must not be empty");
        Assert.notNull(type, "type must not be null");
        Assert.notNull(status, "status must not be null");
        
        ApiDatasource ds = new ApiDatasource(id);
        ds.name = name;
        ds.description = description != null ? description : "";
        ds.type = type;
        ds.status = status;
        ds.operation = operation;
        ds.connection = connection;
        ds.createdAt = createdAt != null ? createdAt : Instant.now();
        ds.updatedAt = updatedAt != null ? updatedAt : Instant.now();
        ds.extensions = extensions != null ? new ArrayList<>(extensions) : new ArrayList<>();
        return ds;
    }

    /**
     * 创建新的 ApiDatasource
     * 
     * @param id 数据源标识（key, version）
     * @return 新创建的 ApiDatasource（状态为 DISABLED）
     */
    public static ApiDatasource create(DatasourceId id) {
        Assert.notNull(id, "DatasourceId must not be null");
        ApiDatasource ds = new ApiDatasource(id);
        ds.domainEvents.add(new ApiDatasourceCreatedEvent(id));
        return ds;
    }

    /* ========= Domain Behavior ========= */

    /**
     * 配置数据源的核心字段
     * 只能在创建后、启用前配置，且必须一次性配置完整
     * 
     * @param type 数据源类型
     * @param name 名称
     * @param description 描述
     * @param operation 操作
     * @param connection 连接规范
     * @throws IllegalStateException 如果已经配置过或已启用
     */
    public void configure(
            DatasourceType type,
            String name,
            String description,
            DatasourceOperation operation,
            DatasourceConnection connection,
            List<Extension> extensions) {
        
        Assert.notNull(type, "type must not be null");
        Assert.hasText(name, "name must not be empty");
        Assert.notNull(operation, "operation must not be null");
        Assert.notNull(connection, "connection must not be null");
        Assert.notNull(extensions, "extensions must not be null");
        
        if (this.status == DatasourceStatus.ENABLED) {
            throw new DatasourceNotConfiguredException(id);
        }
        
        this.type = type;
        this.name = name;
        this.description = description != null ? description : "";
        this.operation = operation;
        this.connection = connection;
        this.extensions = new ArrayList<>(extensions);
        
        touch();
    }

    /**
     * 更新扩展列表
     *
     * @param extensions 新扩展列表
     */
    public void updateExtensions(List<Extension> extensions) {
        Assert.notNull(extensions, "extensions must not be null");
        this.extensions = new ArrayList<>(extensions);
        touch();
    }

    /**
     * 更新可变字段（name、description）
     * 这些字段的修改不影响契约，可以随时修改
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

    public void rename(String name) {
        Assert.hasText(name, "name must not be empty");
        this.name = name;
        touch();
    }

    /**
     * 更新核心字段（operations、connection）
     * 必须确保未被引用（DS-1 规则）
     * 
     * @param isReferenced 是否被引用的检查结果（由应用服务层提供）
     * @param operation 新操作
     * @param connection 新连接规范
     * @throws IllegalStateException 如果被引用或已启用
     */
    public void updateCoreFields(
            boolean isReferenced,
            DatasourceOperation operation,
            DatasourceConnection connection) {
        
        Assert.notNull(operation, "operation must not be null");
        Assert.notNull(connection, "connection must not be null");
        
        if (isReferenced) {
            throw new DatasourceReferencedException(id);
        }
        
        if (this.status == DatasourceStatus.ENABLED) {
            throw new DatasourceNotConfiguredException(id);
        }
        
        this.operation = operation;
        this.connection = connection;
        
        touch();
    }

    /**
     * 启用数据源
     * 只有 Enabled 状态的数据源才允许被调用（DS-2 规则）
     */
    public void enable() {
        if (this.status == DatasourceStatus.ENABLED) {
            return;
        }

        this.status = DatasourceStatus.ENABLED;
        domainEvents.add(new ApiDatasourceEnabledEvent(id));
        touch();
    }

    /**
     * 停用数据源
     */
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

    public boolean isConfigured() {
        return operation != null && connection != null;
    }

    public DatasourceId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public DatasourceType type() {
        return type;
    }

    public DatasourceStatus status() {
        return status;
    }

    public DatasourceOperation operation() {
        return operation;
    }

    public DatasourceConnection connection() {
        return connection;
    }

    public List<Extension> extensions() {
        return List.copyOf(extensions);
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