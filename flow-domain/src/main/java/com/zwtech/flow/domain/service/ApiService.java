package com.zwtech.flow.domain.service;

import com.zwtech.flow.domain.datasource.ApiDatasource;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

/**
 * @author renc
 */
@Builder
@Data
public class ApiService {

    private String id;
    private String name;
    private String category;
    private ApiDatasource datasource;
    private JsonNode inputSchema;
    private JsonNode outputSchema;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
