package com.zwtech.flow.connector.factory.r2dbc;

import com.zwtech.flow.connector.ResponseSpec;
import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class R2dbcResponseSpec implements ResponseSpec {
    private final List<JsonNode> rows;
    private final Map<String, Object> attributes = new HashMap<>();
}