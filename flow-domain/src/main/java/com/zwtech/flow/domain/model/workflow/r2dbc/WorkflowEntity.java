package com.zwtech.flow.domain.model.workflow.r2dbc;

import com.zwtech.flow.domain.model.workflow.*;
import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.*;

/**
 * Workflow 的持久化实体
 *
 * 职责：在领域模型与数据库模型之间翻译语义
 * - 知道 JSONB 存储格式
 * - 知道表结构映射
 * - 知道复杂对象（节点、连接）的序列化/反序列化
 *
 * @author renc
 */
@Data
@Table("flw_workflow")
public class WorkflowEntity {

    @Id
    @Column("id_")
    private Long id;

    @Column("key_")
    private String key;

    @Column("version_")
    private Integer version;

    @Column("name_")
    private String name;

    @Column("description_")
    private String description;

    @Column("status_")
    private String status;

    @Column("nodes_")
    private String nodes;

    @Column("connections_")
    private String connections;

    @Column("config_")
    private String config;

    @Column("created_at_")
    private Instant createdAt;

    @Column("updated_at_")
    private Instant updatedAt;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 将领域模型转换为持久化实体
     */
    public static WorkflowEntity fromWorkflow(Workflow workflow) {
        var entity = new WorkflowEntity();

        entity.setKey(workflow.id().key());
        entity.setVersion(workflow.id().version());
        entity.setName(workflow.name());
        entity.setDescription(workflow.description() != null ? workflow.description() : "");
        entity.setStatus(workflow.status().name());

        // 序列化节点
        try {
            List<NodeData> nodeDataList = new ArrayList<>();
            for (Node node : workflow.nodes().values()) {
                NodeData nodeData = new NodeData();
                nodeData.setId(node.id());
                nodeData.setType(node.type().name());
                nodeData.setName(node.name());
                nodeData.setDatasourceKey(node.datasourceId() != null ? node.datasourceId().key() : null);
                nodeData.setDatasourceVersion(node.datasourceVersion());
                nodeData.setOperationKey(node.operationKey());
                nodeData.setConfig(node.config());
                nodeData.setInputMapping(node.inputMapping());
                nodeData.setOutputMapping(node.outputMapping());
                nodeDataList.add(nodeData);
            }
            entity.setNodes(OBJECT_MAPPER.writeValueAsString(nodeDataList));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize nodes", e);
        }

        // 序列化连接
        try {
            List<ConnectionData> connectionDataList = new ArrayList<>();
            for (Connection conn : workflow.connections().values()) {
                ConnectionData connData = new ConnectionData();
                connData.setId(conn.id());
                connData.setSourceNodeId(conn.sourceNodeId());
                connData.setTargetNodeId(conn.targetNodeId());
                connData.setSourceOutputField(conn.sourceOutputField());
                connData.setTargetInputField(conn.targetInputField());
                connectionDataList.add(connData);
            }
            entity.setConnections(OBJECT_MAPPER.writeValueAsString(connectionDataList));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize connections", e);
        }

        entity.setCreatedAt(workflow.createdAt() != null ? workflow.createdAt() : Instant.now());
        entity.setUpdatedAt(workflow.updatedAt() != null ? workflow.updatedAt() : entity.getCreatedAt());

        return entity;
    }

    /**
     * 从持久化实体恢复领域模型
     */
    public Workflow toWorkflow() {
        WorkflowId workflowId = WorkflowId.of(this.getKey(), this.getVersion());
        WorkflowStatus status = WorkflowStatus.valueOf(this.getStatus());

        // 反序列化节点
        Map<String, Node> nodes = new HashMap<>();
        if (this.getNodes() != null && !this.getNodes().isBlank()) {
            try {
                List<NodeData> nodeDataList = OBJECT_MAPPER.readValue(this.getNodes(),
                        new TypeReference<List<NodeData>>() {});
                for (NodeData nodeData : nodeDataList) {
                    Node node;
                    DatasourceId datasourceId = null;
                    if (nodeData.getDatasourceKey() != null && nodeData.getDatasourceVersion() != null) {
                        datasourceId = new DatasourceId(nodeData.getDatasourceKey(), nodeData.getDatasourceVersion());
                    }

                    if ("DATASOURCE".equals(nodeData.getType())) {
                        node = Node.datasource(
                                nodeData.getId(),
                                nodeData.getName(),
                                datasourceId,
                                nodeData.getOperationKey(),
                                nodeData.getConfig() != null ? nodeData.getConfig() : Map.of(),
                                nodeData.getInputMapping(),
                                nodeData.getOutputMapping()
                        );
                    } else {
                        node = Node.simple(
                                nodeData.getId(),
                                nodeData.getName(),
                                nodeData.getConfig() != null ? nodeData.getConfig() : Map.of(),
                                nodeData.getInputMapping(),
                                nodeData.getOutputMapping()
                        );
                    }
                    nodes.put(node.id(), node);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize nodes", e);
            }
        }

        // 反序列化连接
        Map<String, Connection> connections = new HashMap<>();
        if (this.getConnections() != null && !this.getConnections().isBlank()) {
            try {
                List<ConnectionData> connectionDataList = OBJECT_MAPPER.readValue(this.getConnections(),
                        new TypeReference<List<ConnectionData>>() {});
                for (ConnectionData connData : connectionDataList) {
                    Connection conn = Connection.of(
                            connData.getId(),
                            connData.getSourceNodeId(),
                            connData.getTargetNodeId(),
                            connData.getSourceOutputField(),
                            connData.getTargetInputField()
                    );
                    connections.put(conn.id(), conn);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize connections", e);
            }
        }

        // 使用 WorkflowContract.empty() 作为契约（恢复时不需要完整契约）
        WorkflowContract contract = WorkflowContract.empty();

        return Workflow.restore(
                workflowId,
                this.getName(),
                this.getDescription(),
                status,
                contract,
                nodes,
                connections,
                this.getCreatedAt(),
                this.getUpdatedAt()
        );
    }

    /**
     * 节点数据结构（用于 JSON 序列化）
     */
    @Data
    private static class NodeData {
        private String id;
        private String type;
        private String name;
        private String datasourceKey;
        private Integer datasourceVersion;
        private String operationKey;
        private Map<String, Object> config;
        private String inputMapping;
        private String outputMapping;
    }

    /**
     * 连接数据结构（用于 JSON 序列化）
     */
    @Data
    private static class ConnectionData {
        private String id;
        private String sourceNodeId;
        private String targetNodeId;
        private String sourceOutputField;
        private String targetInputField;
    }
}
