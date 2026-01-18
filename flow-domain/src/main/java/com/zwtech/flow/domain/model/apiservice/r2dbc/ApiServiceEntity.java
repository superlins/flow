package com.zwtech.flow.domain.model.apiservice.r2dbc;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.model.apiservice.ApiService;
import com.zwtech.flow.domain.model.apiservice.FieldBinding;
import com.zwtech.flow.domain.model.apiservice.ServiceContract;
import com.zwtech.flow.domain.model.apiservice.ServiceId;
import com.zwtech.flow.domain.model.apiservice.ServiceMapping;
import com.zwtech.flow.domain.model.apiservice.ServiceStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

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
@Table("flw_api_service")
class ApiServiceEntity {

    @Id
    @Column("id_")
    private Long id;

    @Column("key_")
    private String key;

    @Column("name_")
    private String name;

    @Column("description_")
    private String description;

    @Column("status_")
    private String status;

    @Column("input_schema_")
    private String inputSchema;

    @Column("output_schema_")
    private String outputSchema;

    @Column("datasource_key_")
    private String datasourceKey;

    @Column("datasource_version_")
    private Integer datasourceVersion;

    @Column("input_mapping_")
    private String inputMapping;

    @Column("output_mapping_")
    private String outputMapping;

    @Column("created_at_")
    private Instant createdAt;

    @Column("updated_at_")
    private Instant updatedAt;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 将领域模型转换为持久化实体
     * 
     * 注意：ServiceMapping 需要序列化为 JSON
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
        
        // Mapping（序列化为 JSON）
        if (service.mapping() != null) {
            try {
                Map<String, Object> mappingJson = new HashMap<>();
                Map<String, String> inputMappingMap = new HashMap<>();
                Map<String, String> outputMappingMap = new HashMap<>();

                // 序列化 inputMapping: key 是 targetField，value 是 expression
                for (Map.Entry<String, FieldBinding> entry : service.mapping().inputMapping().entrySet()) {
                    inputMappingMap.put(entry.getKey(), entry.getValue().expression());
                }

                // 序列化 outputMapping: key 是 targetField，value 是 expression
                for (Map.Entry<String, FieldBinding> entry : service.mapping().outputMapping().entrySet()) {
                    outputMappingMap.put(entry.getKey(), entry.getValue().expression());
                }

                mappingJson.put("input", inputMappingMap);
                mappingJson.put("output", outputMappingMap);
                entity.setInputMapping(OBJECT_MAPPER.writeValueAsString(mappingJson));
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize ServiceMapping", e);
            }
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
        
        // Mapping（反序列化）
        ServiceMapping mapping = ServiceMapping.empty();
        if (inputMapping != null && !inputMapping.isEmpty()) {
            try {
                Map<String, Object> mappingJson = OBJECT_MAPPER.readValue(
                        inputMapping,
                        new TypeReference<Map<String, Object>>() {});

                // 检测模式：如果有 workflowId 则为 WORKFLOW 模式
                String workflowIdStr = mappingJson.containsKey("workflowId") ?
                        (String) mappingJson.get("workflowId") : null;
                Integer workflowVersion = mappingJson.containsKey("workflowVersion") ?
                        (Integer) mappingJson.get("workflowVersion") : null;

                Map<String, FieldBinding> inputMapping = new HashMap<>();
                Map<String, FieldBinding> outputMapping = new HashMap<>();

                // 反序列化 inputMapping: key 是 targetField，value 是 expression 字符串
                if (mappingJson.containsKey("input")) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> input =
                            (Map<String, String>) mappingJson.get("input");
                    for (Map.Entry<String, String> entry : input.entrySet()) {
                        String targetField = entry.getKey();
                        String expression = entry.getValue();
                        FieldBinding fieldBinding = new FieldBinding(targetField, expression);
                        inputMapping.put(targetField, fieldBinding);
                    }
                }

                // 反序列化 outputMapping: key 是 targetField，value 是 expression 字符串
                if (mappingJson.containsKey("output")) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> output =
                            (Map<String, String>) mappingJson.get("output");
                    for (Map.Entry<String, String> entry : output.entrySet()) {
                        String targetField = entry.getKey();
                        String expression = entry.getValue();
                        FieldBinding fieldBinding = new FieldBinding(targetField, expression);
                        outputMapping.put(targetField, fieldBinding);
                    }
                }

                // 根据模式创建 ServiceMapping
                if (workflowIdStr != null && !workflowIdStr.isEmpty()) {
                    // WORKFLOW 模式
                    mapping = ServiceMapping.workflow(
                            workflowIdStr,
                            workflowVersion != null ? workflowVersion : 1,
                            inputMapping,
                            outputMapping
                    );
                } else {
                    // DATASOURCE 模式
                    var dsId = datasourceKey != null && datasourceVersion != null ?
                            new DatasourceId(datasourceKey, datasourceVersion) : null;
                    mapping = ServiceMapping.datasource(dsId, datasourceVersion, inputMapping, outputMapping);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize ServiceMapping", e);
            }
        }
        
        // 使用静态工厂方法恢复对象
        return ApiService.restore(
                serviceId,
                name,
                description,
                status,
                contract,
                mapping,
                createdAt,
                updatedAt
        );
    }
}

