package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.shared.ValueObject;

import java.util.Map;
import java.util.Objects;

/**
 * 工作流节点
 * DatasourceMode 节点可以配置失败重试、超时等行为
 *
 * @author renc
 */
public final class Node implements ValueObject<Node> {

    private final String id;
    private final NodeType type;
    private final String name;

    // DatasourceMode 特有字段
    private final DatasourceId datasourceId;
    private final Integer datasourceVersion;
    private final String operationKey;

    // 配置字段（通用）
    private final Map<String, Object> config;
    private final String inputMapping;
    private final String outputMapping;

    private Node(
            String id,
            NodeType type,
            String name,
            DatasourceId datasourceId,
            Integer datasourceVersion,
            String operationKey,
            Map<String, Object> config,
            String inputMapping,
            String outputMapping) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Node id cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Node type cannot be null");
        }
        this.id = id;
        this.type = type;
        this.name = name != null ? name : id;
        this.datasourceId = datasourceId;
        this.datasourceVersion = datasourceVersion;
        this.operationKey = operationKey;
        this.config = config != null ? Map.copyOf(config) : Map.of();
        this.inputMapping = inputMapping;
        this.outputMapping = outputMapping;
    }

    /**
     * 创建 Datasource 节点
     */
    public static Node datasource(
            String id,
            String name,
            DatasourceId datasourceId,
            String operationKey,
            Map<String, Object> config,
            String inputMapping,
            String outputMapping) {
        return new Node(
                id,
                NodeType.DATASOURCE,
                name,
                datasourceId,
                datasourceId.version(),
                operationKey,
                config,
                inputMapping,
                outputMapping
        );
    }

    /**
     * 创建 Simple 节点
     */
    public static Node simple(
            String id,
            String name,
            Map<String, Object> config,
            String inputMapping,
            String outputMapping) {
        return new Node(
                id,
                NodeType.SIMPLE,
                name,
                null,
                null,
                null,
                config,
                inputMapping,
                outputMapping
        );
    }

    public String id() {
        return id;
    }

    public NodeType type() {
        return type;
    }

    public String name() {
        return name;
    }

    public DatasourceId datasourceId() {
        return datasourceId;
    }

    public Integer datasourceVersion() {
        return datasourceVersion;
    }

    public String operationKey() {
        return operationKey;
    }

    public Map<String, Object> config() {
        return Map.copyOf(config);
    }

    public String inputMapping() {
        return inputMapping;
    }

    public String outputMapping() {
        return outputMapping;
    }

    @Override
    public boolean sameValueAs(Node other) {
        if (other == null) return false;
        return id.equals(other.id) &&
                type == other.type &&
                Objects.equals(datasourceId, other.datasourceId) &&
                Objects.equals(datasourceVersion, other.datasourceVersion) &&
                Objects.equals(operationKey, other.operationKey) &&
                Objects.equals(config, other.config) &&
                Objects.equals(inputMapping, other.inputMapping) &&
                Objects.equals(outputMapping, other.outputMapping);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return sameValueAs(node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    /**
     * 节点类型
     */
    public enum NodeType {
        /** 直接调用 Datasource */
        DATASOURCE,
        /** 特殊节点（如输入节点、Merge 节点等） */
        SIMPLE
    }
}
