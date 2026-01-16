package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.shared.ValueObject;
import java.util.Map;
import java.util.Objects;

/**
 * 服务映射规则
 * 用于将Service的输入输出映射到Datasource或Workflow
 */
public final class ServiceMapping implements ValueObject<ServiceMapping> {

    private final ServiceMode mode;

    // Datasource模式时的引用
    private final DatasourceId datasourceId;  // 可以为null
    private final Integer datasourceVersion;    // 可以为null

    // Workflow模式时的引用
    private final String workflowId;            // 可以为null
    private final Integer workflowVersion;      // 可以为null

    // 映射规则（通用）
    private final Map<String, FieldBinding> inputMapping;   // 可以为空map
    private final Map<String, FieldBinding> outputMapping;  // 可以为空map

    /**
     * 运行模式
     */
    public enum ServiceMode {
        DATASOURCE,  // 直接调用Datasource
        WORKFLOW     // 执行Workflow
    }

    private ServiceMapping(
            ServiceMode mode,
            DatasourceId datasourceId,
            Integer datasourceVersion,
            String workflowId,
            Integer workflowVersion,
            Map<String, FieldBinding> inputMapping,
            Map<String, FieldBinding> outputMapping) {
        this.mode = mode;
        this.datasourceId = datasourceId;
        this.datasourceVersion = datasourceVersion;
        this.workflowId = workflowId;
        this.workflowVersion = workflowVersion;
        this.inputMapping = inputMapping != null ? Map.copyOf(inputMapping) : Map.of();
        this.outputMapping = outputMapping != null ? Map.copyOf(outputMapping) : Map.of();
    }

    // === 工厂方法 ===

    /**
     * 创建Datasource模式绑定
     */
    public static ServiceMapping datasource(
            DatasourceId datasourceId,
            Integer datasourceVersion,
            Map<String, FieldBinding> inputMapping,
            Map<String, FieldBinding> outputMapping) {
        return new ServiceMapping(
                ServiceMode.DATASOURCE,
                datasourceId,
                datasourceVersion,
                null,
                null,
                inputMapping,
                outputMapping
        );
    }

    /**
     * 创建Workflow模式绑定
     */
    public static ServiceMapping workflow(
            String workflowId,
            Integer workflowVersion,
            Map<String, FieldBinding> inputMapping,
            Map<String, FieldBinding> outputMapping) {
        return new ServiceMapping(
                ServiceMode.WORKFLOW,
                null,
                null,
                workflowId,
                workflowVersion,
                inputMapping,
                outputMapping
        );
    }

    /**
     * 创建空绑定（未配置状态）
     */
    public static ServiceMapping empty() {
        return new ServiceMapping(
                ServiceMode.DATASOURCE,
                null,
                null,
                null,
                null,
                Map.of(),
                Map.of()
        );
    }

    /**
     * 从JSON字符串恢复（可用于序列化）
     * @deprecated 实现复杂，暂时不支持
     */
    @Deprecated
    public static ServiceMapping fromJson(String json) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // === Getters ===

    public ServiceMode mode() {
        return mode;
    }

    public DatasourceId datasourceId() {
        return datasourceId;
    }

    public Integer datasourceVersion() {
        return datasourceVersion;
    }

    public String workflowId() {
        return workflowId;
    }

    public Integer workflowVersion() {
        return workflowVersion;
    }

    public Map<String, FieldBinding> inputMapping() {
        return Map.copyOf(inputMapping);
    }

    public Map<String, FieldBinding> outputMapping() {
        return Map.copyOf(outputMapping);
    }

    // === ValueObject methods ===

    @Override
    public boolean sameValueAs(ServiceMapping other) {
        if (other == null) return false;
        return mode == other.mode &&
                Objects.equals(datasourceId, other.datasourceId) &&
                Objects.equals(datasourceVersion, other.datasourceVersion) &&
                Objects.equals(workflowId, other.workflowId) &&
                Objects.equals(workflowVersion, other.workflowVersion) &&
                Objects.equals(inputMapping, other.inputMapping) &&
                Objects.equals(outputMapping, other.outputMapping);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceMapping that = (ServiceMapping) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, datasourceId, datasourceVersion, workflowId, workflowVersion, inputMapping, outputMapping);
    }
}
