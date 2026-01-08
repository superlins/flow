package com.zwtech.flow.domain.model.apidatasource.r2dbc;

import com.zwtech.flow.domain.model.apidatasource.*;
import com.zwtech.flow.domain.model.apidatasource.behavior.OperationBehavior;
import com.zwtech.flow.domain.model.apidatasource.connection.DatasourceConnection;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * ApiDatasource 的持久化实体
 * 
 * 职责：在领域模型与数据库模型之间翻译语义
 * - 知道 JSONB 存储格式
 * - 知道表结构映射
 * - 知道多态对象的序列化/反序列化
 * 
 * 领域模型完全不关心这些细节
 *
 * @author renc
 */
@Data
@Table("FLW_API_DATASOURCE")
class ApiDatasourceEntity {

    @Id
    @Column("ID_")
    private Long id;

    @Column("KEY_")
    private String key;

    @Column("VERSION_")
    private Integer version;

    @Column("TYPE_")
    private String type;

    @Column("STATUS_")
    private String status;

    @Column("NAME_")
    private String name;

    @Column("DESCRIPTION_")
    private String description;

    @Column("INPUT_SCHEMA_")
    private String inputSchema;

    @Column("OUTPUT_SCHEMA_")
    private String outputSchema;

    @Column("STRICT_")
    private Boolean strict;

    @Column("OPERATION_")
    private String operation;

    @Column("CONNECTION_")
    private String connection;

    @Column("EXTENSION_")
    private String extension;

    @Column("CREATED_AT_")
    private Instant createdAt;

    @Column("UPDATED_AT_")
    private Instant updatedAt;

    /**
     * 将领域模型转换为持久化实体
     * 
     * 注意：OperationSpec 和 ConnectionSpec 是多态对象，需要序列化为 JSON
     * 当前实现使用简单字符串，后续需要集成 JSON 序列化库（如 Jackson）
     */
    public static ApiDatasourceEntity fromApiDatasource(ApiDatasource ds) {
        var entity = new ApiDatasourceEntity();
        
        // 标识
        entity.setKey(ds.id().key());
        entity.setVersion(ds.id().version());
        
        // 基础信息
        if (ds.type() != null) {
            entity.setType(ds.type().name());
        }
        entity.setStatus(ds.status().name());
        entity.setName(ds.name());
        entity.setDescription(ds.description());
        
        // 契约（JSON Schema 字符串）
        if (ds.contract() != null) {
            entity.setInputSchema(ds.contract().inputSchema());
            entity.setOutputSchema(ds.contract().outputSchema());
            entity.setStrict(ds.contract().strict());
        }
        
        // 操作和连接（需要序列化为 JSON）
        // TODO: 使用 JSON 序列化库（如 Jackson）序列化多态对象
        if (ds.operation() != null) {
            entity.setOperation(ds.operation().toString()); // 临时方案
        }
        if (ds.connection() != null) {
            entity.setConnection(ds.connection().toString()); // 临时方案
        }
        
        // 扩展列表（需要序列化为 JSON 数组）
        if (ds.extensions() != null && !ds.extensions().isEmpty()) {
            // TODO: 序列化为 JSON 数组
            entity.setExtension(ds.extensions().toString()); // 临时方案
        }
        
        // 时间戳
        entity.setCreatedAt(ds.createdAt());
        entity.setUpdatedAt(ds.updatedAt());
        
        return entity;
    }

    /**
     * 将持久化实体转换为领域模型
     * 
     * 注意：需要根据 type 字段反序列化对应的 OperationSpec 和 ConnectionSpec 实现类
     * 当前实现为基础版本，完整的 JSON 序列化/反序列化需要后续完善
     */
    public ApiDatasource toApiDatasource() {
        var datasourceId = new DatasourceId(key, version);
        
        // 基础信息
        DatasourceType datasourceType = type != null ? DatasourceType.valueOf(type) : null;
        DatasourceStatus datasourceStatus = status != null ? DatasourceStatus.valueOf(status) : DatasourceStatus.DISABLED;
        
        // 契约
        OperationContract operationContract = null;
        if (inputSchema != null && outputSchema != null) {
            boolean strictValue = strict != null ? strict : false;
            operationContract = new OperationContract(inputSchema, outputSchema, strictValue);
        }
        
        // 操作和连接（需要反序列化）
        // TODO: 根据 type 反序列化为对应的实现类
        // 例如：HTTP -> HttpOperationSpec, HttpConnectionSpec
        // R2DBC -> SqlOperationSpec, R2dbcConnectionSpec
        // 当前暂时为 null，需要 JSON 反序列化支持
        // 可以使用 Jackson 的 @JsonTypeInfo 和 @JsonSubTypes 实现多态序列化
        var operation = (OperationBehavior) null;
        var connection = (DatasourceConnection) null;
        
        // 扩展列表（需要反序列化）
        // TODO: 从 JSON 数组反序列化为 List<Extension>
        // 可以使用 Jackson 的 ObjectMapper.readValue() 反序列化
        List<OperationExtension> operationExtensions = new ArrayList<>();
        
        // 使用静态工厂方法恢复对象
        return ApiDatasource.restore(
                datasourceId,
                name,
                description,
                datasourceType,
                datasourceStatus, operationContract,
                operation,
                connection, operationExtensions,
                createdAt,
                updatedAt
        );
    }
}