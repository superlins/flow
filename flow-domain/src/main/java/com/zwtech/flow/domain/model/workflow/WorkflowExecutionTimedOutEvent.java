package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * 工作流执行超时事件
 */
public record WorkflowExecutionTimedOutEvent(WorkflowExecutionId executionId, WorkflowId workflowId)
        implements DomainEvent<WorkflowExecutionTimedOutEvent> {

    @Override
    public boolean sameEventAs(WorkflowExecutionTimedOutEvent other) {
        return other != null && Objects.equals(this.executionId, other.executionId);
    }
}
