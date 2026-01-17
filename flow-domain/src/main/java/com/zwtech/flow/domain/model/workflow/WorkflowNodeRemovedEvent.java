package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * 工作流节点移除事件
 */
public record WorkflowNodeRemovedEvent(WorkflowId workflowId, String nodeId)
        implements DomainEvent<WorkflowNodeRemovedEvent> {

    @Override
    public boolean sameEventAs(WorkflowNodeRemovedEvent other) {
        return other != null &&
                Objects.equals(this.workflowId, other.workflowId) &&
                Objects.equals(this.nodeId, other.nodeId);
    }
}