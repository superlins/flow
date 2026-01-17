package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * 工作流版本创建事件
 */
public record WorkflowVersionCreatedEvent(WorkflowId newWorkflowId, WorkflowId previousWorkflowId)
        implements DomainEvent<WorkflowVersionCreatedEvent> {

    @Override
    public boolean sameEventAs(WorkflowVersionCreatedEvent other) {
        return other != null &&
                Objects.equals(this.newWorkflowId, other.newWorkflowId) &&
                Objects.equals(this.previousWorkflowId, other.previousWorkflowId);
    }
}
