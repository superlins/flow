package com.zwtech.flow.domain.model.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.zwtech.flow.domain.shared.DomainEntity;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工作流执行聚合根
 * 一次执行实例，跟踪执行过程和结果
 *
 * @author renc
 */
public final class WorkflowExecution implements DomainEntity<WorkflowExecution> {

    private final WorkflowExecutionId id;

    // 引用的工作流
    private final WorkflowId workflowId;

    // 执行状态
    private WorkflowExecutionStatus status;

    // 输入输出
    private final JsonNode input;
    private JsonNode output;
    private String errorMessage;

    // 节点执行状态
    private final Map<String, NodeExecutionStatus> nodeStatuses;

    // 计时信息
    private Instant startedAt;
    private Instant finishedAt;
    private Duration duration;

    private final List<Object> domainEvents = new ArrayList<>();

    private WorkflowExecution(
            WorkflowExecutionId id,
            WorkflowId workflowId,
            JsonNode input) {
        Assert.notNull(id, "WorkflowExecutionId must not be null");
        Assert.notNull(workflowId, "WorkflowId must not be null");
        Assert.notNull(input, "Input must not be null");

        this.id = id;
        this.workflowId = workflowId;
        this.input = input;
        this.status = WorkflowExecutionStatus.PENDING;
        this.output = null;
        this.errorMessage = null;
        this.nodeStatuses = new HashMap<>();
        this.startedAt = Instant.now();
        this.finishedAt = null;
        this.duration = null;
    }

    /**
     * 创建新的执行实例
     */
    public static WorkflowExecution create(
            WorkflowExecutionId id,
            WorkflowId workflowId,
            JsonNode input) {
        var execution = new WorkflowExecution(id, workflowId, input);
        execution.domainEvents.add(new WorkflowExecutionCreatedEvent(
                id,
                workflowId,
                execution.startedAt
        ));
        return execution;
    }

    /**
     * 从存储恢复
     */
    public static WorkflowExecution restore(
            WorkflowExecutionId id,
            WorkflowId workflowId,
            WorkflowExecutionStatus status,
            JsonNode input,
            JsonNode output,
            String errorMessage,
            Map<String, NodeExecutionStatus> nodeStatuses,
            Instant startedAt,
            Instant finishedAt) {
        Assert.notNull(id, "WorkflowExecutionId must not be null");
        Assert.notNull(workflowId, "WorkflowId must not be null");

        var execution = new WorkflowExecution(id, workflowId, input);
        execution.status = status;
        execution.output = output;
        execution.errorMessage = errorMessage;
        if (nodeStatuses != null) {
            execution.nodeStatuses.putAll(nodeStatuses);
        }
        execution.startedAt = startedAt != null ? startedAt : execution.startedAt;
        execution.finishedAt = finishedAt;
        if (finishedAt != null && startedAt != null) {
            execution.duration = Duration.between(startedAt, finishedAt);
        }
        // 恢复时不发布领域事件
        return execution;
    }

    /**
     * 开始执行
     */
    public void start() {
        if (status != WorkflowExecutionStatus.PENDING) {
            throw new IllegalStateException("Cannot start execution in status: " + status);
        }

        this.status = WorkflowExecutionStatus.RUNNING;
        domainEvents.add(new WorkflowExecutionStartedEvent(id, workflowId));
    }

    /**
     * 标记节点开始执行
     */
    public void markNodeStarted(String nodeId) {
        Assert.hasText(nodeId, "NodeId must not be empty");

        nodeStatuses.put(nodeId, new NodeExecutionStatus(
                nodeId,
                WorkflowExecutionStatus.RUNNING,
                null,
                Instant.now(),
                null
        ));
    }

    /**
     * 标记节点执行完成（成功）
     */
    public void markNodeCompleted(String nodeId, JsonNode output) {
        Assert.hasText(nodeId, "NodeId must not be empty");
        Assert.notNull(output, "Output must not be null");

        var existingStatus = nodeStatuses.get(nodeId);
        nodeStatuses.put(nodeId, new NodeExecutionStatus(
                nodeId,
                WorkflowExecutionStatus.SUCCESS,
                output,
                existingStatus != null ? existingStatus.startedAt : Instant.now(),
                Instant.now()
        ));
    }

    /**
     * 标记节点执行失败
     */
    public void markNodeFailed(String nodeId, String error) {
        Assert.hasText(nodeId, "NodeId must not be empty");
        Assert.hasText(error, "Error message must not be empty");

        var existingStatus = nodeStatuses.get(nodeId);
        nodeStatuses.put(nodeId, new NodeExecutionStatus(
                nodeId,
                WorkflowExecutionStatus.FAILED,
                null,
                existingStatus != null ? existingStatus.startedAt : Instant.now(),
                Instant.now()
        ));

        // 某个节点失败时，执行失败
        this.status = WorkflowExecutionStatus.FAILED;
        this.errorMessage = "Node " + nodeId + " failed: " + error;

        domainEvents.add(new WorkflowExecutionFailedEvent(
                id,
                workflowId,
                this.errorMessage
        ));
    }

    /**
     * 完成执行（成功）
     */
    public void complete(JsonNode output) {
        if (status != WorkflowExecutionStatus.RUNNING) {
            throw new IllegalStateException("Cannot complete execution in status: " + status);
        }

        Assert.notNull(output, "Output must not be null");

        this.status = WorkflowExecutionStatus.SUCCESS;
        this.output = output;
        this.finishedAt = Instant.now();
        this.duration = Duration.between(startedAt, finishedAt);

        domainEvents.add(new WorkflowExecutionCompletedEvent(
                id,
                workflowId,
                output,
                duration
        ));
    }

    /**
     * 完成执行（失败）
     */
    public void fail(String error) {
        if (status != WorkflowExecutionStatus.RUNNING && status != WorkflowExecutionStatus.PENDING) {
            throw new IllegalStateException("Cannot fail execution in status: " + status);
        }

        Assert.hasText(error, "Error message must not be empty");

        this.status = WorkflowExecutionStatus.FAILED;
        this.errorMessage = error;
        this.finishedAt = Instant.now();
        this.duration = Duration.between(startedAt, finishedAt);

        domainEvents.add(new WorkflowExecutionFailedEvent(
                id,
                workflowId,
                error
        ));
    }

    /**
     * 超时
     */
    public void timeout() {
        if (status != WorkflowExecutionStatus.RUNNING && status != WorkflowExecutionStatus.PENDING) {
            throw new IllegalStateException("Cannot timeout execution in status: " + status);
        }

        this.status = WorkflowExecutionStatus.TIMEOUT;
        this.errorMessage = "Execution timed out";
        this.finishedAt = Instant.now();
        this.duration = Duration.between(startedAt, finishedAt);

        domainEvents.add(new WorkflowExecutionTimedOutEvent(id, workflowId));
    }

    /**
     * 取消执行
     */
    public void cancel() {
        if (status == WorkflowExecutionStatus.SUCCESS ||
            status == WorkflowExecutionStatus.FAILED ||
            status == WorkflowExecutionStatus.CANCELED) {
            throw new IllegalStateException("Cannot cancel execution in status: " + status);
        }

        this.status = WorkflowExecutionStatus.CANCELED;
        this.finishedAt = Instant.now();
        if (startedAt != null) {
            this.duration = Duration.between(startedAt, finishedAt);
        }

        domainEvents.add(new WorkflowExecutionCanceledEvent(id, workflowId));
    }

    // Getters

    @Override
    public boolean sameIdentityAs(WorkflowExecution other) {
        return other != null && this.id.equals(other.id);
    }

    public WorkflowExecutionId id() {
        return id;
    }

    public WorkflowId workflowId() {
        return workflowId;
    }

    public WorkflowExecutionStatus status() {
        return status;
    }

    public JsonNode input() {
        return input;
    }

    public JsonNode output() {
        return output;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public Map<String, NodeExecutionStatus> nodeStatuses() {
        return Collections.unmodifiableMap(nodeStatuses);
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant finishedAt() {
        return finishedAt;
    }

    public Duration duration() {
        return duration;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof WorkflowExecution other) && sameIdentityAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 节点执行状态值对象
     */
    public static record NodeExecutionStatus(
            String nodeId,
            WorkflowExecutionStatus status,
            JsonNode output,
            Instant startedAt,
            Instant completedAt
    ) {}
}
