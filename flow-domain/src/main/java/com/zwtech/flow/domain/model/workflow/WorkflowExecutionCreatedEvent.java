package com.zwtech.flow.domain.model.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.zwtech.flow.domain.shared.DomainEvent;

import java.time.Instant;
import java.time.Duration;
import java.util.Objects;

/**
 * 工作流执行创建事件
 */
public record WorkflowExecutionCreatedEvent(WorkflowExecutionId executionId, WorkflowId workflowId, Instant startedAt)
        implements DomainEvent<WorkflowExecutionCreatedEvent> {

    @Override
    public boolean sameEventAs(WorkflowExecutionCreatedEvent other) {
        return other != null &&
                Objects.equals(this.executionId, other.executionId) &&
                Objects.equals(this.startedAt, other.startedAt);
    }
}