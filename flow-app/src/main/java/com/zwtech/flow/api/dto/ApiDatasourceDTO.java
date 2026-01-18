package com.zwtech.flow.api.dto;

import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;

/**
 * ApiDatasource DTO for API responses
 *
 * @author renc
 */
public record ApiDatasourceDTO(
    String id,
    String key,
    int version,
    String name,
    String description,
    String type,
    String status,
    String inputSchema,
    String outputSchema,
    boolean strict,
    String connection,
    String createdAt,
    String updatedAt
) {
    public static ApiDatasourceDTO fromApiDatasource(ApiDatasource datasource) {
        return new ApiDatasourceDTO(
            datasource.id().key() + ":" + datasource.id().version(),
            datasource.id().key(),
            datasource.id().version(),
            datasource.name(),
            datasource.description(),
            datasource.type() != null ? datasource.type().name() : null,
            datasource.status() != null ? datasource.status().name() : "DISABLED",
            datasource.contract() != null ? datasource.contract().inputSchema() : null,
            datasource.contract() != null ? datasource.contract().outputSchema() : null,
            datasource.contract() != null ? datasource.contract().strict() : false,
            datasource.connection() != null ? datasource.connection().toString() : null,
            datasource.createdAt() != null ? datasource.createdAt().toString() : null,
            datasource.updatedAt() != null ? datasource.updatedAt().toString() : null
        );
    }
}
