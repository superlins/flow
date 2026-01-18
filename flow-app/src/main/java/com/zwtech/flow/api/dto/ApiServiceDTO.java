package com.zwtech.flow.api.dto;

import com.zwtech.flow.domain.model.apiservice.ApiService;

/**
 * ApiService DTO for API responses
 *
 * @author renc
 */
public record ApiServiceDTO(
        String id,
        String key,
        String name,
        String description,
        String status,
        String mode,
        String datasourceId,
        Integer datasourceVersion,
        String workflowId,
        Integer workflowVersion,
        String inputSchema,
        String outputSchema,
        String inputMapping,
        String outputMapping,
        String createdAt,
        String updatedAt
) {
    public static ApiServiceDTO fromApiService(ApiService service) {
        return new ApiServiceDTO(
                service.id().value(),
                service.id().value(),
                service.name(),
                service.description(),
                service.status() != null ? service.status().name() : "DISABLED",
                service.mapping() != null && service.mapping().mode() != null ? service.mapping().mode().name() : null,
                service.datasourceId() != null ? service.datasourceId().key() : null,
                service.datasourceId() != null ? service.datasourceId().version() : null,
                service.workflowId(),
                service.mapping() != null ? service.mapping().workflowVersion() : null,
                service.contract() != null ? service.contract().inputSchema() : null,
                service.contract() != null ? service.contract().outputSchema() : null,
                service.mapping() != null ? service.mapping().inputMapping().toString() : null,
                service.mapping() != null ? service.mapping().outputMapping().toString() : null,
                service.createdAt() != null ? service.createdAt().toString() : null,
                service.updatedAt() != null ? service.updatedAt().toString() : null
        );
    }
}
