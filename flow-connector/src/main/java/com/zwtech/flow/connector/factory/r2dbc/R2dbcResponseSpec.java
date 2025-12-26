package com.zwtech.flow.connector.factory.r2dbc;

import org.example.core.connector.ResponseSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class R2dbcResponseSpec implements ResponseSpec {
    private List<Map<String, Object>> rows;
    private final Map<String, Object> attributes = new HashMap<>();

    public R2dbcResponseSpec rows(List<Map<String, Object>> rows) {
        this.rows = rows;
        return this;
    }

    public R2dbcResponseSpec attribute(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }

    public List<Map<String, Object>> getRows() { return rows; }
    public Map<String, Object> getAttributes() { return attributes; }
}