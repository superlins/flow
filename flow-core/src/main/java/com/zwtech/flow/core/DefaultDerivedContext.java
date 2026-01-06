package com.zwtech.flow.core;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * DerivedContext 的不可变实现。
 * 默认使用 JSON Pointer 语法访问/更新路径（如 "/a/b"）。
 *
 * @author renc
 */
public final class DefaultDerivedContext implements DerivedContext {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JsonNode root;

    public DefaultDerivedContext(JsonNode root) {
        this.root = root == null ? MAPPER.createObjectNode() : root.deepCopy();
    }

    @Override
    public JsonNode get(String path) {
        return root.at(path);
    }

    @Override
    public boolean contains(String path) {
        return !root.at(path).isMissingNode();
    }

    @Override
    public DerivedContext with(String path, JsonNode value) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(value, "value must not be null");
        ObjectNode copy = root != null && root.isObject() ? ((ObjectNode) root).deepCopy() : MAPPER.createObjectNode();
        // JSON Pointer set 支持有限，这里简化为仅支持对象路径
        // 若路径不存在则逐级创建
        String[] segments = path.startsWith("/") ? path.substring(1).split("/") : new String[]{path};
        ObjectNode cursor = copy;
        for (int i = 0; i < segments.length; i++) {
            String seg = segments[i];
            if (i == segments.length - 1) {
                cursor.set(seg, value);
            } else {
                JsonNode next = cursor.get(seg);
                if (!(next instanceof ObjectNode)) {
                    next = MAPPER.createObjectNode();
                    cursor.set(seg, next);
                }
                cursor = (ObjectNode) next;
            }
        }
        return new DefaultDerivedContext(copy);
    }

    @Override
    public String toString() {
        return root.toString();
    }
}

