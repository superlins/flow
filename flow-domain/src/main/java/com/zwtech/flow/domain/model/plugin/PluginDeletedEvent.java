package com.zwtech.flow.domain.model.plugin;

import java.time.Instant;

/**
 * 插件删除事件（归档后删除）
 */
public class PluginDeletedEvent {
    private final PluginId pluginId;
    private final Instant deletedAt;

    public PluginDeletedEvent(PluginId pluginId) {
        this.pluginId = pluginId;
        this.deletedAt = Instant.now();
    }

    public PluginId pluginId() { return pluginId; }
    public Instant deletedAt() { return deletedAt; }
}
