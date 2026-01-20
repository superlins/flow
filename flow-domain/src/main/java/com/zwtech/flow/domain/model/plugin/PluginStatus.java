package com.zwtech.flow.domain.model.plugin;

/**
 * 插件状态枚举（业务状态）
 * <p>
 * 注意：这只是领域模型中的业务状态，不等同于 SpringPluginManager 的插件状态
 */
public enum PluginStatus {
    ENABLED("启用", "插件已启用，处于活动状态"),
    DISABLED("禁用", "插件已禁用，不参与业务处理"),
    ARCHIVED("归档", "插件已归档，不再使用");

    private final String code;
    private final String description;

    PluginStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }
}
