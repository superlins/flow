package com.zwtech.flow.domain.model.plugin;

import java.time.Instant;

/**
 * 插件已启用事件
 * <p>
 * 事件订阅者（基础设施层）应该：
 * 1. 如果是 JAR 插件：调用 SpringPluginManager.startPlugin(pluginId)
 * 2. 如果是脚本插件：加载脚本引擎并执行脚本初始化
 */
public class PluginEnabledEvent {
    private final PluginId pluginId;
    private final Instant enabledAt;

    public PluginEnabledEvent(PluginId pluginId, Instant enabledAt) {
        this.pluginId = pluginId;
        this.enabledAt = enabledAt;
    }

    public PluginId pluginId() { return pluginId; }
    public Instant enabledAt() { return enabledAt; }
}
