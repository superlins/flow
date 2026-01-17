package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * 工作流执行失败事件
 */
public record WorkflowExecutionFailedEvent(WorkflowExecutionId executionId, WorkflowId workflowId, String errorMessage)
        implements DomainEvent<WorkflowExecutionFailedEvent> {

    @Override
    public boolean sameEventAs(WorkflowExecutionFailedEvent other) {
        return other != null &&
                Objects.equals(this.executionId, other.executionId) &&
                Objects.equals(this.errorMessage, other.errorMessage);
    }
}
