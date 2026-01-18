package com.zwtech.flow.service;

import com.zwtech.flow.core.plugin.SpringPluginManager;
import com.zwtech.flow.domain.model.apidatasource.DatasourceType;
import com.zwtech.flow.domain.model.apidatasource.plugin.DatasourcePlugin;
import com.zwtech.flow.domain.model.apidatasource.plugin.DatasourcePluginMetadata;
import com.zwtech.flow.domain.service.PluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin Registry 默认实现
 * <p>
 * 使用 ConcurrentHashMap 存储插件，保证线程安全。
 * 集成 PF4J 插件管理器，支持插件自动发现和加载。
 *
 * @author renc
 */
public class DefaultPluginRegistry implements PluginRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPluginRegistry.class);

    private final SpringPluginManager pluginManager;
    private final Map<String, DatasourcePlugin> plugins = new ConcurrentHashMap<>();
    private final Map<String, DatasourcePluginMetadata> metadataCache = new ConcurrentHashMap<>();

    public DefaultPluginRegistry(SpringPluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public void register(DatasourcePlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin must not be null");
        }

        DatasourcePluginMetadata metadata = plugin.getMetadata();
        if (metadata == null) {
            throw new IllegalArgumentException("plugin metadata must not be null");
        }

        String pluginId = metadata.id();
        if (plugins.containsKey(pluginId)) {
            logger.warn("Plugin {} is already registered", pluginId);
            return;
        }

        logger.info("Registering plugin: {} (version: {})", metadata.name(), metadata.version());

        try {
            // Initialize the plugin
            plugin.initialize();

            // Store the plugin and metadata
            plugins.put(pluginId, plugin);
            metadataCache.put(pluginId, metadata);

            logger.info("Plugin {} registered successfully", pluginId);
        } catch (Exception e) {
            logger.error("Failed to initialize plugin {}", pluginId, e);
            throw new IllegalStateException("Failed to initialize plugin: " + pluginId, e);
        }
    }

    @Override
    public boolean unregister(String pluginId) {
        if (pluginId == null) {
            return false;
        }

        DatasourcePlugin plugin = plugins.remove(pluginId);
        if (plugin == null) {
            logger.warn("Plugin {} not found for unregistration", pluginId);
            return false;
        }

        logger.info("Unregistering plugin: {}", pluginId);

        try {
            // Shutdown the plugin
            plugin.shutdown();
            metadataCache.remove(pluginId);

            logger.info("Plugin {} unregistered successfully", pluginId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to shutdown plugin {}", pluginId, e);
            return false;
        }
    }

    @Override
    public Optional<DatasourcePlugin> findById(String pluginId) {
        if (pluginId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(plugins.get(pluginId));
    }

    @Override
    public Optional<DatasourcePlugin> findByType(DatasourceType type) {
        if (type == null) {
            return Optional.empty();
        }

        return plugins.values().stream()
                .filter(plugin -> plugin.supports(type))
                .filter(DatasourcePlugin::isReady)
                .findFirst();
    }

    @Override
    public List<DatasourcePlugin> listPlugins() {
        return List.copyOf(plugins.values());
    }

    @Override
    public List<DatasourcePluginMetadata> listMetadata() {
        return List.copyOf(metadataCache.values());
    }

    @Override
    public boolean isTypeReady(DatasourceType type) {
        if (type == null) {
            return false;
        }

        return plugins.values().stream()
                .anyMatch(plugin ->
                        plugin.supports(type) && plugin.isReady());
    }

    @Override
    public boolean exists(String pluginId) {
        if (pluginId == null) {
            return false;
        }
        return plugins.containsKey(pluginId);
    }

    @Override
    public void clear() {
        logger.warn("Clearing all plugins from registry");
        plugins.clear();
        metadataCache.clear();
    }

    /**
     * 自动从 PF4J 加载所有 DatasourcePlugin 扩展
     * <p>
     * 通常在应用启动时调用，扫描所有已加载的插件并注册。
     */
    public void autoRegisterFromPluginManager() {
        logger.info("Auto-registering plugins from PF4J plugin manager");

        var pluginExtensions = pluginManager.getExtensions(DatasourcePlugin.class);
        if (pluginExtensions.isEmpty()) {
            logger.info("No datasource plugin extensions found");
            return;
        }

        for (DatasourcePlugin plugin : pluginExtensions) {
            try {
                register(plugin);
            } catch (Exception e) {
                logger.error("Failed to auto-register plugin", e);
            }
        }

        logger.info("Auto-registration completed. Total plugins: {}", plugins.size());
    }
}