package com.zwtech.flow.connector.factory.r2dbc;

import com.zwtech.flow.connector.RequestSpec;
import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@Builder
@Getter
public class R2dbcRequestSpec implements RequestSpec {
    private String sql;
    private final JsonNode parameters;
    private final Map<String, Object> attributes = new HashMap<>();
}