package com.zwtech.flow.domain.model.plugin;

/**
 * 插件内容（值对象）
 * <p>
 * 根据插件类型不同，内容结构也不同
 */
public sealed interface PluginContent {

    /**
     * JAR 插件内容
     */
    record JarContent(
            String jarPath,
            String pluginClassName
    ) implements PluginContent {
        public JarContent {
            if (jarPath == null) {
                throw new IllegalArgumentException("jarPath must not be null");
            }
            if (pluginClassName == null) {
                throw new IllegalArgumentException("pluginClassName must not be null");
            }
        }
    }

    /**
     * 脚本插件内容
     */
    record ScriptContent(
            ScriptLanguage language,
            String scriptBody
    ) implements PluginContent {
        public ScriptContent {
            if (language == null) {
                throw new IllegalArgumentException("language must not be null");
            }
            if (scriptBody == null) {
                throw new IllegalArgumentException("scriptBody must not be null");
            }
        }
    }

    /**
     * 内联代码插件内容
     */
    record InlineContent(
            String code,
            String language
    ) implements PluginContent {
        public InlineContent {
            if (code == null) {
                throw new IllegalArgumentException("code must not be null");
            }
        }
    }

    // 工厂方法
    static JarContent jar(String jarPath, String pluginClassName) {
        return new JarContent(jarPath, pluginClassName);
    }

    static ScriptContent script(ScriptLanguage language, String scriptBody) {
        return new ScriptContent(language, scriptBody);
    }

    static InlineContent inline(String code, String language) {
        return new InlineContent(code, language);
    }
}
