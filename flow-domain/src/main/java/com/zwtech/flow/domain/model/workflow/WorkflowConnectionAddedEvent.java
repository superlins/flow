package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * 工作流连接添加事件
 */
public record WorkflowConnectionAddedEvent(WorkflowId workflowId, String connectionId)
        implements DomainEvent<WorkflowConnectionAddedEvent> {

    @Override
    public boolean sameEventAs(WorkflowConnectionAddedEvent other) {
        return other != null &&
                Objects.equals(this.workflowId, other.workflowId) &&
                Objects.equals(this.connectionId, other.connectionId);
    }
}
