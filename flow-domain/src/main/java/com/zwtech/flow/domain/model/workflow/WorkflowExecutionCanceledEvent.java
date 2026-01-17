package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * 工作流执行取消事件
 */
public record WorkflowExecutionCanceledEvent(WorkflowExecutionId executionId, WorkflowId workflowId)
        implements DomainEvent<WorkflowExecutionCanceledEvent> {

    @Override
    public boolean sameEventAs(WorkflowExecutionCanceledEvent other) {
        return other != null && Objects.equals(this.executionId, other.executionId);
    }
}
