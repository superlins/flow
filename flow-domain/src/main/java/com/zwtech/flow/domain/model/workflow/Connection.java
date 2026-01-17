package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.ValueObject;

import java.util.Objects;

/**
 * 连接关系
 * 定义节点之间的 DAG 边
 *
 * @author renc
 */
public final class Connection implements ValueObject<Connection> {

    private final String id;
    private final String sourceNodeId;
    private final String targetNodeId;
    private final String sourceOutputField;
    private final String targetInputField;

    private Connection(String id, String sourceNodeId, String targetNodeId,
                      String sourceOutputField, String targetInputField) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Connection id cannot be null or empty");
        }
        if (sourceNodeId == null || sourceNodeId.isBlank()) {
            throw new IllegalArgumentException("Source node id cannot be null or empty");
        }
        if (targetNodeId == null || targetNodeId.isBlank()) {
            throw new IllegalArgumentException("Target node id cannot be null or empty");
        }
        if (sourceNodeId.equals(targetNodeId)) {
            throw new IllegalArgumentException("Source and target nodes cannot be the same");
        }
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.sourceOutputField = sourceOutputField;
        this.targetInputField = targetInputField;
    }

    /**
     * 创建完整连接（含字段绑定）
     */
    public static Connection of(
            String id,
            String sourceNodeId,
            String targetNodeId,
            String sourceOutputField,
            String targetInputField) {
        return new Connection(id, sourceNodeId, targetNodeId, sourceOutputField, targetInputField);
    }

    /**
     * 创建节点连接（无字段绑定，默认字段名为 "output" 和 "input"）
     */
    public static Connection ofNodes(String id, String sourceNodeId, String targetNodeId) {
        return new Connection(id, sourceNodeId, targetNodeId, "output", "input");
    }

    public String id() {
        return id;
    }

    public String sourceNodeId() {
        return sourceNodeId;
    }

    public String targetNodeId() {
        return targetNodeId;
    }

    public String sourceOutputField() {
        return sourceOutputField;
    }

    public String targetInputField() {
        return targetInputField;
    }

    @Override
    public boolean sameValueAs(Connection other) {
        if (other == null) return false;
        return id.equals(other.id) &&
                sourceNodeId.equals(other.sourceNodeId) &&
                targetNodeId.equals(other.targetNodeId) &&
                Objects.equals(sourceOutputField, other.sourceOutputField) &&
                Objects.equals(targetInputField, other.targetInputField);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection connection = (Connection) o;
        return sameValueAs(connection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
