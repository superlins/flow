package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.DomainEvent;

import java.util.Objects;

/**
 * 工作流元数据更新事件
 */
public record WorkflowMetadataUpdatedEvent(WorkflowId workflowId, String name, String description)
        implements DomainEvent<WorkflowMetadataUpdatedEvent> {

    @Override
    public boolean sameEventAs(WorkflowMetadataUpdatedEvent other) {
        return other != null &&
                Objects.equals(this.workflowId, other.workflowId) &&
                Objects.equals(this.name, other.name) &&
                Objects.equals(this.description, other.description);
    }
}
