package com.zwtech.flow.domain.model.apidatasource.r2dbc;

import com.zwtech.flow.domain.model.apidatasource.*;
import com.zwtech.flow.domain.model.apidatasource.operation.DatasourceOperation;
import com.zwtech.flow.domain.model.apidatasource.connection.DatasourceConnection;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 将领域模型转换为持久化实体
     * 
     * 注意：Operations（Map<String, DatasourceOperation>）和 Connection 是多态对象，需要序列化为 JSON
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
        
        // Operation（序列化为 JSON）
        try {
            if (ds.operation() != null) {
                // 使用 Jackson 序列化 DatasourceOperation
                // 注意：需要 DatasourceOperation 实现类支持 JSON 序列化
                entity.setOperation(OBJECT_MAPPER.writeValueAsString(ds.operation()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize operation", e);
        }
        
        // Connection（序列化为 JSON）
        try {
            if (ds.connection() != null) {
                entity.setConnection(OBJECT_MAPPER.writeValueAsString(ds.connection()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize connection", e);
        }
        
        // 扩展列表（序列化为 JSON 数组）
        try {
            if (ds.extensions() != null && !ds.extensions().isEmpty()) {
                entity.setExtension(OBJECT_MAPPER.writeValueAsString(ds.extensions()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize extensions", e);
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
        DatasourceContract datasourceContract = null;
        if (inputSchema != null && outputSchema != null) {
            boolean strictValue = strict != null ? strict : false;
            datasourceContract = new DatasourceContract(inputSchema, outputSchema, strictValue);
        }
        
        // Operation（反序列化）
        DatasourceOperation datasourceOperation = null;
        try {
            if (operation != null && !operation.isEmpty()) {
                // 注意：需要根据 type 反序列化为对应的实现类
                // 例如：HTTP -> HttpDatasourceOperation
                // R2DBC -> SqlDatasourceOperation
                // 可以使用 Jackson 的 @JsonTypeInfo 和 @JsonSubTypes 实现多态序列化
                // 当前实现需要 DatasourceOperation 实现类支持 JSON 反序列化
                // TODO: 根据 type 反序列化为具体的 DatasourceOperation 实现
                // 当前暂时为 null，需要完整的 JSON 反序列化支持
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize operation", e);
        }
        
        // Connection（反序列化）
        DatasourceConnection connection = null;
        try {
            if (this.connection != null && !this.connection.isEmpty()) {
                // 注意：需要根据 type 反序列化为对应的实现类
                // 例如：HTTP -> HttpDatasourceConnection
                // R2DBC -> R2dbcDatasourceConnection
                // 可以使用 Jackson 的 @JsonTypeInfo 和 @JsonSubTypes 实现多态序列化
                // 当前暂时为 null，需要完整的 JSON 反序列化支持
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize connection", e);
        }
        
        // 扩展列表（反序列化）
        List<Extension> extensions = new ArrayList<>();
        try {
            if (extension != null && !extension.isEmpty()) {
                extensions = OBJECT_MAPPER.readValue(
                        extension, 
                        new TypeReference<List<Extension>>() {});
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize extensions", e);
        }
        
        // 使用静态工厂方法恢复对象
        return ApiDatasource.restore(
                datasourceId,
                name,
                description,
                datasourceType,
                datasourceStatus,
                datasourceContract,
                datasourceOperation,
                connection,
                extensions,
                createdAt,
                updatedAt
        );
    }
}