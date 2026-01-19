package com.zwtech.flow.connector.factory.r2dbc;

import com.zwtech.flow.connector.RequestSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * R2DBC 请求规格
 *
 * @author renc
 */
public class R2dbcRequestSpec implements RequestSpec {

    private final String sql;
    private final JsonNode parameters;
    private final Map<String, Object> attributes = new HashMap<>();

    private R2dbcRequestSpec(String sql, JsonNode parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    public String getSql() {
        return sql;
    }

    /**
     * 获取参数的 JsonNode 格式（用于 Binder/Converter）
     */
    public JsonNode getParameters() {
        return parameters;
    }

    /**
     * 获取参数列表（用于 Connector 绑定）
     * 将 JsonNode 转换为 List<Object>
     */
    public List<Object> getParametersAsList() {
        if (!(parameters instanceof ArrayNode arrayNode)) {
            return List.of();
        }
        List<Object> result = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            if (node.isTextual()) {
                result.add(node.asText());
            } else if (node.isNumber()) {
                if (node.isInt()) {
                    result.add(node.asInt());
                } else if (node.isLong()) {
                    result.add(node.asLong());
                } else {
                    result.add(node.asDouble());
                }
            } else if (node.isBoolean()) {
                result.add(node.asBoolean());
            } else {
                result.add(node.toString());
            }
        }
        return result;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sql;
        private JsonNode parameters;

        public Builder sql(String sql) {
            this.sql = sql;
            return this;
        }

        public Builder parameters(JsonNode parameters) {
            this.parameters = parameters;
            return this;
        }

        public R2dbcRequestSpec build() {
            return new R2dbcRequestSpec(sql, parameters);
        }
    }
}
