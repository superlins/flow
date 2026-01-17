package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * 工作流创建事件
 */
public record WorkflowCreatedEvent(WorkflowId workflowId, String workflowName, Instant occurredAt)
        implements DomainEvent<WorkflowCreatedEvent> {

    @Override
    public boolean sameEventAs(WorkflowCreatedEvent other) {
        return other != null &&
                Objects.equals(this.workflowId, other.workflowId) &&
                Objects.equals(this.occurredAt, other.occurredAt);
    }
}
