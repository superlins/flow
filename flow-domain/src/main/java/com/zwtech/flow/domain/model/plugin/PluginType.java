package com.zwtech.flow.domain.model.plugin;

/**
 * 插件类型枚举
 */
public enum PluginType {
    JAR("JAR", "PF4J 标准插件 JAR 文件"),
    SCRIPT("SCRIPT", "脚本插件（Groovy/JavaScript 等）"),
    INLINE("INLINE", "内联代码插件");

    private final String code;
    private final String description;

    PluginType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
