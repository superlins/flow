package com.zwtech.flow.domain.model.plugin;

import java.time.Instant;

/**
 * 插件已禁用事件
 * <p>
 * 事件订阅者应该：
 * 1. 如果是 JAR 插件：调用 SpringPluginManager.stopPlugin(pluginId)
 * 2. 如果是脚本插件：卸载脚本或清理资源
 */
public class PluginDisabledEvent {
    private final PluginId pluginId;
    private final Instant disabledAt;

    public PluginDisabledEvent(PluginId pluginId, Instant disabledAt) {
        this.pluginId = pluginId;
        this.disabledAt = disabledAt;
    }

    public PluginId pluginId() { return pluginId; }
    public Instant disabledAt() { return disabledAt; }
}
