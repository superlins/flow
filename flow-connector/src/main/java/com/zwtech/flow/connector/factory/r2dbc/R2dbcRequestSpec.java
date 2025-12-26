package com.zwtech.flow.connector.factory.r2dbc;

import org.example.core.connector.RequestSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class R2dbcRequestSpec implements RequestSpec {
    private String sql;
    private final List<Object> parameters = new ArrayList<>();
    private final Map<String, Object> attributes = new HashMap<>();

    public R2dbcRequestSpec sql(String sql) {
        this.sql = sql;
        return this;
    }

    public R2dbcRequestSpec param(Object value) {
        this.parameters.add(value);
        return this;
    }

    public R2dbcRequestSpec attribute(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }

    public String getSql() { return sql; }
    public List<Object> getParameters() { return parameters; }
    public Map<String, Object> getAttributes() { return attributes; }
}