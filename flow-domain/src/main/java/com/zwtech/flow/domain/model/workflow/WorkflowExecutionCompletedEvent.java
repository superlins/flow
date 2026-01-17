package com.zwtech.flow.domain.model.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.zwtech.flow.domain.shared.DomainEvent;

import java.time.Duration;
import java.util.Objects;

/**
 * 工作流执行完成事件
 */
public record WorkflowExecutionCompletedEvent(
        WorkflowExecutionId executionId,
        WorkflowId workflowId,
        JsonNode output,
        Duration duration)
        implements DomainEvent<WorkflowExecutionCompletedEvent> {

    @Override
    public boolean sameEventAs(WorkflowExecutionCompletedEvent other) {
        return other != null &&
                Objects.equals(this.executionId, other.executionId) &&
                Objects.equals(this.duration, other.duration);
    }
}
