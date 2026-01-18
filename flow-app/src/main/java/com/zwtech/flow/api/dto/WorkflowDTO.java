package com.zwtech.flow.api.dto;

import com.zwtech.flow.domain.model.workflow.Workflow;

/**
 * Workflow DTO for API responses
 * Separate from domain model to avoid exposing internal structure
 *
 * @author renc
 */
public record WorkflowDTO(
    String id,
    String key,
    int version,
    String name,
    String description,
    String status,
    String createdAt,
    String updatedAt
) {
    public static WorkflowDTO fromWorkflow(Workflow workflow) {
        return new WorkflowDTO(
            workflow.id().key() + ":" + workflow.id().version(),
            workflow.id().key(),
            workflow.id().version(),
            workflow.name(),
            workflow.description(),
            workflow.status() != null ? workflow.status().name() : "DRAFT",
            workflow.createdAt() != null ? workflow.createdAt().toString() : null,
            workflow.updatedAt() != null ? workflow.updatedAt().toString() : null
        );
    }
}
