package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * 工作流连接移除事件
 */
public record WorkflowConnectionRemovedEvent(WorkflowId workflowId, String connectionId)
        implements DomainEvent<WorkflowConnectionRemovedEvent> {

    @Override
    public boolean sameEventAs(WorkflowConnectionRemovedEvent other) {
        return other != null &&
                Objects.equals(this.workflowId, other.workflowId) &&
                Objects.equals(this.connectionId, other.connectionId);
    }
}
