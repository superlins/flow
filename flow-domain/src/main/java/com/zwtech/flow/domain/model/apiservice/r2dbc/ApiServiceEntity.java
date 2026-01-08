package com.zwtech.flow.domain.model.apiservice.r2dbc;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.model.apiservice.ApiService;
import com.zwtech.flow.domain.model.apiservice.ServiceContract;
import com.zwtech.flow.domain.model.apiservice.ServiceId;
import com.zwtech.flow.domain.model.apiservice.ServiceStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * ApiService 的持久化实体
 * 
 * 职责：在领域模型与数据库模型之间翻译语义
 * - 知道 JSONB 存储格式
 * - 知道表结构映射
 * - 知道复杂对象的序列化/反序列化
 * 
 * 领域模型完全不关心这些细节
 *
 * @author renc
 */
@Data
@Table("FLW_API_SERVICE")
class ApiServiceEntity {

    @Id
    @Column("ID_")
    private Long id;

    @Column("KEY_")
    private String key;

    @Column("NAME_")
    private String name;

    @Column("DESCRIPTION_")
    private String description;

    @Column("STATUS_")
    private String status;

    @Column("INPUT_SCHEMA_")
    private String inputSchema;

    @Column("OUTPUT_SCHEMA_")
    private String outputSchema;

    @Column("DATASOURCE_KEY_")
    private String datasourceKey;

    @Column("DATASOURCE_VERSION_")
    private Integer datasourceVersion;

    @Column("BINDING_SPEC_")
    private String bindingSpec;

    @Column("CREATED_AT_")
    private Instant createdAt;

    @Column("UPDATED_AT_")
    private Instant updatedAt;

    /**
     * 将领域模型转换为持久化实体
     * 
     * 注意：BindingSpec 需要序列化为 JSON
     * 当前实现使用简单字符串，后续需要集成 JSON 序列化库（如 Jackson）
     */
    public static ApiServiceEntity fromApiService(ApiService service) {
        var entity = new ApiServiceEntity();
        
        // 标识
        entity.setKey(service.id().value());
        
        // 基础信息
        entity.setName(service.name());
        entity.setDescription(service.description());
        entity.setStatus(service.status().name());
        
        // 契约（JSON Schema 字符串）
        if (service.contract() != null) {
            entity.setInputSchema(service.contract().inputSchema());
            entity.setOutputSchema(service.contract().outputSchema());
        }
        
        // Datasource 引用
        if (service.datasourceId() != null) {
            entity.setDatasourceKey(service.datasourceId().key());
            entity.setDatasourceVersion(service.datasourceId().version());
        }
        
        // 时间戳
        entity.setCreatedAt(service.createdAt());
        entity.setUpdatedAt(service.updatedAt());
        
        return entity;
    }

    /**
     * 将持久化实体转换为领域模型
     * 
     * 注意：需要反序列化 BindingSpec
     */
    public ApiService toApiService() {
        var serviceId = new ServiceId(this.key);
        
        // 基础信息
        var status = ServiceStatus.valueOf(this.status);
        
        // 契约
        var contract = new ServiceContract(inputSchema, outputSchema);
        
        // Datasource 引用
        var datasourceId = new DatasourceId(datasourceKey, datasourceVersion);
        
        // 使用静态工厂方法恢复对象
        return ApiService.restore(
                serviceId,
                name,
                description,
                status,
                contract,
                datasourceId,
                createdAt,
                updatedAt
        );
    }
}

