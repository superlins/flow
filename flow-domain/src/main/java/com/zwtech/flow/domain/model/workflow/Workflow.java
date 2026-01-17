package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.DomainEntity;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工作流聚合根
 * 负责管理节点、连接关系，确保 DAG 结构约束
 *
 * @author renc
 */
public final class Workflow implements DomainEntity<Workflow> {

    private final WorkflowId id;
    private String name;
    private String description;
    private WorkflowStatus status;
    private final WorkflowContract contract;
    private final Map<String, Node> nodes;
    private final Map<String, Connection> connections;

    private final List<Object> domainEvents = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;

    private Workflow(WorkflowId id) {
        Assert.notNull(id, "WorkflowId must not be null");
        this.id = id;
        this.status = WorkflowStatus.DRAFT;
        this.contract = WorkflowContract.empty();
        this.nodes = new HashMap<>();
        this.connections = new HashMap<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * 创建新工作流
     */
    public static Workflow create(
            WorkflowId id,
            String name,
            String description,
            WorkflowContract contract) {
        Assert.hasText(name, "Workflow name must not be empty");

        var workflow = new Workflow(id);
        workflow.name = name;
        workflow.description = description;
        // Workflow 对象上的 contract 是 final 的，这里从业务角度理解为新创建
        // 实际应用中 contract 可能在对象创建时设置，但这里需要更灵活的处理

        workflow.domainEvents.add(new WorkflowCreatedEvent(
                id,
                name,
                workflow.createdAt()
        ));
        return workflow;
    }

    /**
     * 从存储恢复
     */
    public static Workflow restore(
            WorkflowId id,
            String name,
            String description,
            WorkflowStatus status,
            WorkflowContract contract,
            Map<String, Node> nodes,
            Map<String, Connection> connections,
            Instant createdAt,
            Instant updatedAt) {
        var workflow = new Workflow(id);
        workflow.name = name;
        workflow.description = description;
        workflow.status = status != null ? status : WorkflowStatus.DRAFT;
        workflow.nodes.putAll(nodes != null ? nodes : Map.of());
        workflow.connections.putAll(connections != null ? connections : Map.of());
        workflow.createdAt = createdAt;
        workflow.updatedAt = updatedAt;
        // 注意：恢复时不发布领域事件，因为这是从数据库重建，不是新的业务操作
        return workflow;
    }

    /**
     * 启用工作流
     * 规则 WF-1: 仅 DRAFT 或 DISABLED 状态可以启用
     */
    public void enable() {
        if (status != WorkflowStatus.DRAFT && status != WorkflowStatus.DISABLED) {
            throw new IllegalStateException(
                    "Cannot enable workflow in status: " + status);
        }

        this.status = WorkflowStatus.ENABLED;
        this.updatedAt = Instant.now();

        domainEvents.add(new WorkflowStatusChangedEvent(id, status, WorkflowStatus.ENABLED));
    }

    /**
     * 停用工作流
     * 规则 WF-2: 仅 ENABLED 状态可以停用
     */
    public void disable() {
        if (status != WorkflowStatus.ENABLED) {
            throw new IllegalStateException(
                    "Cannot disable workflow in status: " + status);
        }

        this.status = WorkflowStatus.DISABLED;
        this.updatedAt = Instant.now();

        domainEvents.add(new WorkflowStatusChangedEvent(id, status, WorkflowStatus.DISABLED));
    }

    /**
     * 归档工作流
     * 规则 WF-3: 仅 DISABLED 状态可以归档
     */
    public void archive() {
        if (status != WorkflowStatus.DISABLED) {
            throw new IllegalStateException(
                    "Cannot archive workflow in status: " + status);
        }

        this.status = WorkflowStatus.ARCHIVED;
        this.updatedAt = Instant.now();

        domainEvents.add(new WorkflowStatusChangedEvent(id, status, WorkflowStatus.ARCHIVED));
    }

    /**
     * 更新元数据
     * 规则 WF-4: 仅 DRAFT 或 DISABLED 状态可以修改元数据
     */
    public void updateMetadata(String name, String description) {
        if (status != WorkflowStatus.DRAFT && status != WorkflowStatus.DISABLED) {
            throw new IllegalStateException(
                    "Cannot update metadata in status: " + status);
        }

        this.name = name != null ? name : this.name;
        this.description = description;
        this.updatedAt = Instant.now();

        domainEvents.add(new WorkflowMetadataUpdatedEvent(id, this.name, this.description));
    }

    /**
     * 更新契约
     * 规则 WF-5: 仅 DRAFT 状态可更新契约
     */
    public void updateContract(WorkflowContract contract) {
        if (status != WorkflowStatus.DRAFT) {
            throw new IllegalStateException(
                    "Cannot update contract in status: " + status);
        }

        // Workflow 对象的 contract 是 final 的，但这里我们从业务角度理解
        // 实际上会创建新版本的 Workflow 对象
        throw new UnsupportedOperationException("Use workflow.nextVersion() to update contract");
    }

    /**
     * 添加节点
     * WF-8: 添加节点不会破坏 DAG（单节点永远安全）
     */
    public void addNode(Node node) {
        if (status != WorkflowStatus.DRAFT && status != WorkflowStatus.DISABLED) {
            throw new IllegalStateException(
                    "Cannot add node in status: " + status);
        }

        if (nodes.containsKey(node.id())) {
            throw new IllegalArgumentException("Node already exists: " + node.id());
        }

        nodes.put(node.id(), node);
        this.updatedAt = Instant.now();

        domainEvents.add(new WorkflowNodeAddedEvent(id, node.id()));
    }

    /**
     * 移除节点
     * 规则 WF-9: 移除节点后的图仍然是 DAG
     */
    public void removeNode(String nodeId) {
        if (status != WorkflowStatus.DRAFT && status != WorkflowStatus.DISABLED) {
            throw new IllegalStateException(
                    "Cannot remove node in status: " + status);
        }

        if (!nodes.containsKey(nodeId)) {
            throw new IllegalArgumentException("Node not found: " + nodeId);
        }

        // 移除节点及相关连接
        nodes.remove(nodeId);
        connections.entrySet().removeIf(entry ->
                entry.getValue().sourceNodeId().equals(nodeId) ||
                entry.getValue().targetNodeId().equals(nodeId));

        this.updatedAt = Instant.now();

        domainEvents.add(new WorkflowNodeRemovedEvent(id, nodeId));
    }

    /**
     * 添加连接
     * 规则 WF-6: 不允许创建循环
     * 规则 WF-7: 不允许重复边
     */
    public void addConnection(Connection connection) {
        if (status != WorkflowStatus.DRAFT && status != WorkflowStatus.DISABLED) {
            throw new IllegalStateException(
                    "Cannot add connection in status: " + status);
        }

        if (!nodes.containsKey(connection.sourceNodeId())) {
            throw new IllegalArgumentException("Source node not found: " + connection.sourceNodeId());
        }

        if (!nodes.containsKey(connection.targetNodeId())) {
            throw new IllegalArgumentException("Target node not found: " + connection.targetNodeId());
        }

        // 检查是否已存在相同连接
        if (connections.values().stream()
                .anyMatch(c -> c.sourceNodeId().equals(connection.sourceNodeId()) &&
                        c.targetNodeId().equals(connection.targetNodeId()))) {
            throw new IllegalStateException("Connection already exists between these nodes");
        }

        // 检查是否会产生循环
        if (wouldCreateCycle(connection.sourceNodeId(), connection.targetNodeId())) {
            throw new IllegalStateException("Connection would create a cycle");
        }

        connections.put(connection.id(), connection);
        this.updatedAt = Instant.now();

        domainEvents.add(new WorkflowConnectionAddedEvent(id, connection.id()));
    }

    /**
     * 移除连接
     */
    public void removeConnection(String connectionId) {
        if (status != WorkflowStatus.DRAFT && status != WorkflowStatus.DISABLED) {
            throw new IllegalStateException(
                    "Cannot remove connection in status: " + status);
        }

        if (!connections.containsKey(connectionId)) {
            throw new IllegalArgumentException("Connection not found: " + connectionId);
        }

        connections.remove(connectionId);
        this.updatedAt = Instant.now();

        domainEvents.add(new WorkflowConnectionRemovedEvent(id, connectionId));
    }

    /**
     * 创建新版本工作流
     * 规则 WF-10: 使用 enable() 前必须调用 nextVersion()
     */
    public Workflow nextVersion() {
        var newId = id.nextVersion();
        var now = Instant.now();
        var newWorkflow = restore(
                newId,
                this.name + " (v" + newId.version() + ")",
                this.description,
                WorkflowStatus.DRAFT,
                this.contract,
                new HashMap<>(this.nodes),
                new HashMap<>(this.connections),
                now,
                now
        );

        newWorkflow.domainEvents.add(new WorkflowVersionCreatedEvent(newId, id));

        return newWorkflow;
    }

    /**
     * 检测添加连接是否会创建循环
     */
    private boolean wouldCreateCycle(String sourceNodeId, String targetNodeId) {
        // 检查从 targetNodeId 是否可以到达 sourceNodeId
        // 如果能到达，说明添加这条边会形成循环
        return canReach(targetNodeId, sourceNodeId);
    }

    /**
     * 检测从 startId 是否可以到达 targetId（DFS）
     */
    private boolean canReach(String startId, String targetId) {
        if (startId.equals(targetId)) {
            return true;
        }

        for (Connection conn : connections.values()) {
            if (conn.sourceNodeId().equals(startId)) {
                if (canReach(conn.targetNodeId(), targetId)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Getters

    @Override
    public boolean sameIdentityAs(Workflow other) {
        return other != null && this.id.equals(other.id);
    }

    public WorkflowId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public WorkflowStatus status() {
        return status;
    }

    public WorkflowContract contract() {
        return contract;
    }

    public Map<String, Node> nodes() {
        return Collections.unmodifiableMap(nodes);
    }

    public Map<String, Connection> connections() {
        return Collections.unmodifiableMap(connections);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Workflow other) && sameIdentityAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
