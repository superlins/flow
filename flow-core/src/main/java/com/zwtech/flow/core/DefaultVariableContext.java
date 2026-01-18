package com.zwtech.flow.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认变量上下文实现
 *
 * @author renc
 */
public final class DefaultVariableContext implements VariableContext {

    private final JsonNode request;
    private JsonNode response;
    private final Map<String, Object> variables;
    private final boolean isRoot;

    public DefaultVariableContext(JsonNode request, JsonNode response) {
        this.request = request;
        this.response = response;
        this.variables = new ConcurrentHashMap<>();
        this.isRoot = true;
    }

    private DefaultVariableContext(JsonNode request, JsonNode response,
                                  Map<String, Object> variables, boolean isRoot) {
        this.request = request;
        this.response = response;
        this.variables = variables;
        this.isRoot = isRoot;
    }

    @Override
    public Optional<JsonNode> getRequest() {
        return Optional.ofNullable(request).filter(n -> !n.isMissingNode());
    }

    @Override
    public Optional<JsonNode> getResponse() {
        return Optional.ofNullable(response).filter(n -> !n.isMissingNode());
    }

    @Override
    public Optional<JsonNode> getRequestAt(String path) {
        return getRequest().map(req -> getAtPath(req, path));
    }

    @Override
    public Optional<JsonNode> getResponseAt(String path) {
        return getResponse().map(resp -> getAtPath(resp, path));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getVariable(String name, Class<T> type) {
        return Optional.ofNullable((T) variables.get(name));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getVariableOrDefault(String name, Class<T> type, T defaultValue) {
        Object value = variables.get(name);
        return value != null && type.isInstance(value) ? (T) value : defaultValue;
    }

    @Override
    public <T> void setVariable(String name, T value) {
        if (isRoot) {
            variables.put(name, value);
        } else {
            throw new IllegalStateException("Cannot set variable on child context");
        }
    }

    @Override
    public Map<String, Object> getVariables() {
        return Map.copyOf(variables);
    }

    @Override
    public VariableContext createChild() {
        return new DefaultVariableContext(request, response, variables, false);
    }

    @Override
    public VariableContext withResponse(JsonNode newResponse) {
        return new DefaultVariableContext(request, newResponse, variables, isRoot);
    }

    /**
     * 按路径获取节点（简化实现）
     * 支持 "user", "user.id", "data.items[0]" 等简单路径
     */
    private JsonNode getAtPath(JsonNode node, String path) {
        if (path == null || path.isEmpty()) {
            return node;
        }

        String[] parts = path.split("\\.");
        JsonNode current = node;

        for (String part : parts) {
            if (current == null || current.isMissingNode()) {
                return MissingNode.getInstance();
            }

            // 处理数组索引，如 items[0]
            if (part.contains("[") && part.endsWith("]")) {
                String fieldName = part.substring(0, part.indexOf("["));
                String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);

                if (!fieldName.isEmpty()) {
                    current = current.get(fieldName);
                }

                if (current != null && current.isArray()) {
                    try {
                        int index = Integer.parseInt(indexStr);
                        if (index >= 0 && index < current.size()) {
                            current = current.get(index);
                        } else {
                            return MissingNode.getInstance();
                        }
                    } catch (NumberFormatException e) {
                        return MissingNode.getInstance();
                    }
                } else {
                    return MissingNode.getInstance();
                }
            } else {
                current = current.get(part);
            }
        }

        return current != null ? current : MissingNode.getInstance();
    }
}
