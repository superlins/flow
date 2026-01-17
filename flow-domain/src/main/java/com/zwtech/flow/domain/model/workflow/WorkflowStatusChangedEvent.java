package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * 工作流状态变更事件
 */
public record WorkflowStatusChangedEvent(WorkflowId workflowId, WorkflowStatus fromStatus, WorkflowStatus toStatus)
        implements DomainEvent<WorkflowStatusChangedEvent> {

    @Override
    public boolean sameEventAs(WorkflowStatusChangedEvent other) {
        return other != null &&
                Objects.equals(this.workflowId, other.workflowId) &&
                Objects.equals(this.fromStatus, other.fromStatus) &&
                Objects.equals(this.toStatus, other.toStatus);
    }
}
