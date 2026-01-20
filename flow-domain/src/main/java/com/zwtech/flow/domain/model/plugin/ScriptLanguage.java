package com.zwtech.flow.domain.model.plugin;

/**
 * 脚本语言枚举
 */
public enum ScriptLanguage {
    GROOVY("groovy", "Groovy"),
    JAVASCRIPT("javascript", "JavaScript"),
    PYTHON("python", "Python");

    private final String extension;
    private final String displayName;

    ScriptLanguage(String extension, String displayName) {
        this.extension = extension;
        this.displayName = displayName;
    }

    public String getExtension() {
        return extension;
    }

    public String getDisplayName() {
        return displayName;
    }
}
