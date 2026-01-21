package com.zwtech.flow.core.persistent.r2dbc.workflow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zwtech.flow.domain.model.workflow.WorkflowExecution;
import com.zwtech.flow.domain.model.workflow.WorkflowExecutionId;
import com.zwtech.flow.domain.model.workflow.WorkflowExecutionStatus;
import com.zwtech.flow.domain.model.workflow.WorkflowId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * WorkflowExecution 的持久化实体
 *
 * 职责：在领域模型与数据库模型之间翻译语义
 * - 知道 JSONB 存储格式
 * - 知道表结构映射
 * - 知道复杂对象（节点状态）的序列化/反序列化
 *
 * @author renc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("flw_workflow_execution")
public class WorkflowExecutionEntity {

    @Id
    @Column("id_")
    private Long id;

    @Column("execution_id_")
    private String executionId;

    @Column("workflow_key_")
    private String workflowKey;

    @Column("workflow_version_")
    private Integer workflowVersion;

    @Column("status_")
    private String status;

    @Column("input_")
    private String input;

    @Column("output_")
    private String output;

    @Column("error_message_")
    private String errorMessage;

    @Column("node_statuses_")
    private String nodeStatuses;

    @Column("started_at_")
    private Instant startedAt;

    @Column("finished_at_")
    private Instant finishedAt;

    @Column("duration_ms_")
    private Long durationMs;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 将领域模型转换为持久化实体
     */
    public static WorkflowExecutionEntity fromWorkflowExecution(WorkflowExecution execution) {
        var entity = new WorkflowExecutionEntity();

        entity.setExecutionId(execution.id().value());
        entity.setWorkflowKey(execution.workflowId().key());
        entity.setWorkflowVersion(execution.workflowId().version());
        entity.setStatus(execution.status().name());

        // 序列化输入
        try {
            entity.setInput(OBJECT_MAPPER.writeValueAsString(execution.input()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize input", e);
        }

        // 序列化输出
        if (execution.output() != null) {
            try {
                entity.setOutput(OBJECT_MAPPER.writeValueAsString(execution.output()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize output", e);
            }
        }

        entity.setErrorMessage(execution.errorMessage());

        // 序列化节点状态
        try {
            Map<String, NodeStatusData> nodeStatusDataMap = new HashMap<>();
            for (var entry : execution.nodeStatuses().entrySet()) {
                NodeStatusData nodeStatusData = NodeStatusData.builder()
                        .nodeId(entry.getValue().nodeId())
                        .status(entry.getValue().status().name())
                        .output(entry.getValue().output() != null ?
                                OBJECT_MAPPER.writeValueAsString(entry.getValue().output()) : null)
                        .startedAt(entry.getValue().startedAt())
                        .completedAt(entry.getValue().completedAt())
                        .build();
                nodeStatusDataMap.put(entry.getKey(), nodeStatusData);
            }
            if (!nodeStatusDataMap.isEmpty()) {
                entity.setNodeStatuses(OBJECT_MAPPER.writeValueAsString(nodeStatusDataMap));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize node statuses", e);
        }

        entity.setStartedAt(execution.startedAt());
        entity.setFinishedAt(execution.finishedAt());
        entity.setDurationMs(execution.duration() != null ? execution.duration().toMillis() : null);

        return entity;
    }

    /**
     * 从持久化实体恢复领域模型
     */
    public WorkflowExecution toWorkflowExecution() {
        WorkflowExecutionId executionId = WorkflowExecutionId.of(this.executionId);
        WorkflowId workflowId = WorkflowId.of(this.workflowKey, this.workflowVersion);
        WorkflowExecutionStatus status = WorkflowExecutionStatus.valueOf(this.status);

        // 反序列化输入
        JsonNode input = null;
        if (this.input != null && !this.input.isBlank()) {
            try {
                input = OBJECT_MAPPER.readTree(this.input);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize input", e);
            }
        }

        // 反序列化输出
        JsonNode output = null;
        if (this.output != null && !this.output.isBlank()) {
            try {
                output = OBJECT_MAPPER.readTree(this.output);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize output", e);
            }
        }

        // 反序列化节点状态
        Map<String, WorkflowExecution.NodeExecutionStatus> nodeStatuses = new HashMap<>();
        if (this.nodeStatuses != null && !this.nodeStatuses.isBlank()) {
            try {
                Map<String, NodeStatusData> nodeStatusDataMap = OBJECT_MAPPER.readValue(this.nodeStatuses,
                        new TypeReference<Map<String, NodeStatusData>>() {});
                for (var entry : nodeStatusDataMap.entrySet()) {
                    JsonNode nodeOutput = null;
                    if (entry.getValue().output != null && !entry.getValue().output.isBlank()) {
                        nodeOutput = OBJECT_MAPPER.readTree(entry.getValue().output);
                    }

                    nodeStatuses.put(entry.getKey(), new WorkflowExecution.NodeExecutionStatus(
                            entry.getValue().nodeId,
                            WorkflowExecutionStatus.valueOf(entry.getValue().status),
                            nodeOutput,
                            entry.getValue().startedAt,
                            entry.getValue().completedAt
                    ));
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize node statuses", e);
            }
        }

        // 计算持续时间
        Duration duration = null;
        if (this.durationMs != null) {
            duration = Duration.ofMillis(this.durationMs);
        }

        return WorkflowExecution.restore(
                executionId,
                workflowId,
                status,
                input,
                output,
                this.errorMessage,
                nodeStatuses,
                this.startedAt,
                this.finishedAt
        );
    }

    /**
     * 节点状态数据结构（用于 JSON 序列化）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeStatusData {
        private String nodeId;
        private String status;
        private String output;
        private Instant startedAt;
        private Instant completedAt;
    }
}
