package com.zwtech.flow.config;

import com.zwtech.flow.core.plugin.SpringPluginManager;
import com.zwtech.flow.domain.service.PluginRegistry;
import com.zwtech.flow.service.DefaultPluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Plugin Configuration
 * <p>
 * Configures the plugin system including:
 * - SpringPluginManager for PF4J integration
 * - PluginRegistry for managing datasource plugins
 * - Auto-registration of discovered plugins
 *
 * @author renc
 */
@Configuration
@ConditionalOnProperty(name = "flow.plugins.enabled", havingValue = "true", matchIfMissing = true)
public class PluginConfig {

    private static final Logger logger = LoggerFactory.getLogger(PluginConfig.class);

    /**
     * Create SpringPluginManager bean
     * <p>
     * Uses flow.plugins.root property for plugin directory.
     * Defaults to ./plugins if not specified.
     */
    @Bean
    public SpringPluginManager pluginManager() {
        String pluginsRoot = System.getProperty("flow.plugins.root", "./plugins");
        Path pluginsPath = Paths.get(pluginsRoot);

        logger.info("Initializing PF4J Plugin Manager with plugins root: {}", pluginsPath.toAbsolutePath());

        SpringPluginManager pluginManager = new SpringPluginManager(pluginsPath);
        return pluginManager;
    }

    /**
     * Create PluginRegistry bean
     * <p>
     * Automatically registers all discovered DatasourcePlugin extensions.
     */
    @Bean
    public PluginRegistry pluginRegistry(SpringPluginManager pluginManager) {
        logger.info("Creating Plugin Registry");

        DefaultPluginRegistry registry = new DefaultPluginRegistry(pluginManager);

        // Auto-register plugins from PF4J plugin manager
        try {
            registry.autoRegisterFromPluginManager();
            logger.info("Plugin Registry initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to auto-register plugins from PF4J", e);
        }

        return registry;
    }
}