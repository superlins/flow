package com.zwtech.flow.domain.model.plugin;

import java.time.Instant;

/**
 * 插件已创建事件
 */
public class PluginCreatedEvent {
    private final PluginId pluginId;
    private final PluginType type;
    private final Instant createdAt;

    public PluginCreatedEvent(PluginId pluginId, PluginType type) {
        this.pluginId = pluginId;
        this.type = type;
        this.createdAt = Instant.now();
    }

    public PluginId pluginId() { return pluginId; }
    public PluginType type() { return type; }
    public Instant createdAt() { return createdAt; }
}
