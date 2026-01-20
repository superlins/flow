package com.zwtech.flow.domain.model.plugin;

import com.zwtech.flow.domain.shared.DomainEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Plugin 领域聚合根
 * <p>
 * 这是插件在领域层中的**声明式定义**，只包含插件的业务元数据。
 * 真正的插件生命周期管理由基础设施层的 SpringPluginManager 负责。
 * <p>
 * 设计原则：
 * - 领域层不依赖 SpringPluginManager，保持纯粹的业务逻辑
 * - 通过事件驱动机制，将状态变化通知给基础设施层
 * - 插件类型可以是：JAR（标准 PF4J 插件）、脚本、Inline 代码等
 * <p>
 * 生命周期状态机：
 * <pre>
 *   DISABLED → ENABLED
 *     ↓
 *   LOADED → STARTED → STOPPED → UNLOADED
 * </pre>
 * 上面是业务状态和基础设施状态的正交：
 * - 业务状态：ENABLED/DISABLED（用户是否启用）
 * - 基础设施状态：LOADED/STARTED/STOPPED/UNLOADED（插件管理器状态）
 *
 * @author renc
 */
public final class Plugin implements DomainEntity<Plugin> {

    private final PluginId id;
    private String name;
    private String version;
    private String description;
    private PluginType type;
    private PluginStatus status;

    // 插件内容（根据类型不同）
    private final PluginContent content;

    // 插件配置
    private Map<String, Object> config;

    // 扩展信息（如支持的 DatasourceType、提供的 Filters）
    private List<Class<?>> extensionPoints;

    private final List<Object> domainEvents = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
    private Instant enabledAt;
    private Instant disabledAt;

    private Plugin(PluginId id, PluginType type, PluginContent content) {
        this.id = Objects.requireNonNull(id, "PluginId must not be null");
        this.type = Objects.requireNonNull(type, "PluginType must not be null");
        this.content = Objects.requireNonNull(content, "PluginContent must not be null");
        this.status = PluginStatus.DISABLED;
        this.config = Map.of();
        this.extensionPoints = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;

        // 创建事件
        this.domainEvents.add(new PluginCreatedEvent(id, type));
    }

    /**
     * 创建 JAR 类型插件
     */
    public static Plugin createJarPlugin(
            PluginId id,
            String name,
            String version,
            String description,
            String jarPath,
            String pluginClassName
    ) {
        var content = PluginContent.jar(jarPath, pluginClassName);
        var plugin = new Plugin(id, PluginType.JAR, content);
        plugin.name = name;
        plugin.version = version;
        plugin.description = description;
        return plugin;
    }

    /**
     * 创建脚本类型插件（Groovy/JavaScript/etc）
     */
    public static Plugin createScriptPlugin(
            PluginId id,
            String name,
            String version,
            String description,
            ScriptLanguage language,
            String scriptBody
    ) {
        var content = PluginContent.script(language, scriptBody);
        var plugin = new Plugin(id, PluginType.SCRIPT, content);
        plugin.name = name;
        plugin.version = version;
        plugin.description = description;
        return plugin;
    }

    /**
     * 启用插件
     * <p>
     * 触发 PluginEnabledEvent，由事件订阅者调用 SpringPluginManager.startPlugin()
     *
     * @throws IllegalStateException 如果插件已经是启用状态
     */
    public void enable() {
        if (this.status == PluginStatus.ENABLED) {
            return; // 幂等操作
        }

        this.status = PluginStatus.ENABLED;
        this.enabledAt = Instant.now();
        this.updatedAt = Instant.now();

        this.domainEvents.add(new PluginEnabledEvent(id, enabledAt));
    }

    /**
     * 禁用插件
     * <p>
     * 触发 PluginDisabledEvent，由事件订阅者调用 SpringPluginManager.stopPlugin()
     */
    public void disable() {
        if (this.status == PluginStatus.DISABLED) {
            return;
        }

        this.status = PluginStatus.DISABLED;
        this.disabledAt = Instant.now();
        this.updatedAt = Instant.now();

        this.domainEvents.add(new PluginDisabledEvent(id, disabledAt));
    }

    /**
     * 更新插件元数据（name, description, config 等）
     * <p>
     * 注意：type 和 content 不允许更新（需要卸载后重新创建）
     */
    public void updateMetadata(String name, String description, Map<String, Object> config) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.description = description;
        this.config = config != null ? Map.copyOf(config) : this.config;
        this.updatedAt = Instant.now();
    }

    /**
     * 注册扩展点声明
     * <p>
     * 例如：插件的 ConnectorFilter 扩展点
     */
    public void registerExtensionPoint(Class<?> extensionClass) {
        if (extensionClass == null) {
            throw new IllegalArgumentException("extensionClass must not be null");
        }
        if (!this.extensionPoints.contains(extensionClass)) {
            this.extensionPoints.add(extensionClass);
        }
    }

    /**
     * 获取所有扩展点声明
     */
    public List<Class<?>> getExtensionPoints() {
        return List.copyOf(extensionPoints);
    }

    // ========= Getters =========

    public PluginId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    public String description() {
        return description;
    }

    public PluginType type() {
        return type;
    }

    public PluginStatus status() {
        return status;
    }

    public PluginContent content() {
        return content;
    }

    public Map<String, Object> config() {
        return Map.copyOf(config);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Instant enabledAt() {
        return enabledAt;
    }

    public Instant disabledAt() {
        return disabledAt;
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    @Override
    public boolean sameIdentityAs(Plugin other) {
        return other != null && this.id.sameValueAs(other.id);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Plugin other) && sameIdentityAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
