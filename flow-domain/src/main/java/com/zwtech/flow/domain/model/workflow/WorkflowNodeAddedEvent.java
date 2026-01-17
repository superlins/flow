package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * 工作流节点添加事件
 */
public record WorkflowNodeAddedEvent(WorkflowId workflowId, String nodeId)
        implements DomainEvent<WorkflowNodeAddedEvent> {

    @Override
    public boolean sameEventAs(WorkflowNodeAddedEvent other) {
        return other != null &&
                Objects.equals(this.workflowId, other.workflowId) &&
                Objects.equals(this.nodeId, other.nodeId);
    }
}
