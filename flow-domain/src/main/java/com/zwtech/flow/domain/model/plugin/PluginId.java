package com.zwtech.flow.domain.model.plugin;

import com.zwtech.flow.domain.shared.ValueObject;

import java.util.Objects;

/**
 * 插件 ID 值对象
 */
public record PluginId(String value) implements ValueObject<PluginId> {
    public PluginId {
        Objects.requireNonNull(value, "PluginId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PluginId value must not be blank");
        }
    }

    public static PluginId of(String value) {
        return new PluginId(value);
    }

    @Override
    public boolean sameValueAs(PluginId other) {
        return other != null && this.value.equals(other.value);
    }
}
