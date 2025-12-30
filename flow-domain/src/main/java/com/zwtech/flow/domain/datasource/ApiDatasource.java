package com.zwtech.flow.domain.datasource;

import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author renc
 */
@Builder
@Data
public class ApiDatasource {

    private String id;
    private String name;
    private String type;
    private String version;
    private JsonNode specification;
    private JsonNode connection;
    private JsonNode extension;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
