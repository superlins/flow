package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * 工作流执行开始事件
 */
public record WorkflowExecutionStartedEvent(WorkflowExecutionId executionId, WorkflowId workflowId)
        implements DomainEvent<WorkflowExecutionStartedEvent> {

    @Override
    public boolean sameEventAs(WorkflowExecutionStartedEvent other) {
        return other != null && Objects.equals(this.executionId, other.executionId);
    }
}