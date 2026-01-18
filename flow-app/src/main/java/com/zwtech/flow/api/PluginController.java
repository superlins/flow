package com.zwtech.flow.api;

import com.zwtech.flow.api.dto.PluginDTO;
import com.zwtech.flow.domain.model.apidatasource.plugin.DatasourcePlugin;
import com.zwtech.flow.domain.model.apidatasource.plugin.DatasourcePluginMetadata;
import com.zwtech.flow.domain.service.PluginRegistry;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Plugin Management REST API
 * <p>
 * 提供 DatasourcePlugin 的管理功能:
 * - 列出所有插件及其状态
 * - 获取插件详情
 * - 加载/卸载插件
 * - 启动/停止插件
 *
 * @author renc
 */
@RestController
@RequestMapping("/api/plugins")
public class PluginController {

    private static final Logger logger = LoggerFactory.getLogger(PluginController.class);

    private final PluginRegistry pluginRegistry;

    public PluginController(PluginRegistry pluginRegistry) {
        this.pluginRegistry = pluginRegistry;
    }

    /**
     * 列出所有插件
     */
    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> listPlugins() {
        return Mono.fromCallable(() -> {
            var allPlugins = pluginRegistry.listMetadata()
                    .stream()
                    .map(PluginDTO::fromMetadata)
                    .collect(Collectors.toList());

            Map<String, Object> result = Map.of(
                    "plugins", allPlugins,
                    "total", allPlugins.size()
            );

            return ResponseEntity.ok(result);
        });
    }

    /**
     * 获取插件详情
     */
    @GetMapping("/{pluginId}")
    public Mono<ResponseEntity<PluginDTO>> getPlugin(@PathVariable String pluginId) {
        return Mono.fromCallable(() -> pluginRegistry.findById(pluginId))
                .flatMap(pluginOpt -> pluginOpt
                        .map(plugin -> Mono.just(ResponseEntity.ok(PluginDTO.fromPlugin(plugin))))
                        .orElse(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))));
    }

    /**
     * 启动插件（如果已加载但未启动）
     */
    @PostMapping("/{pluginId}/start")
    public Mono<ResponseEntity<PluginDTO>> startPlugin(@PathVariable String pluginId) {
        return Mono.fromCallable(() -> {
            DatasourcePlugin plugin = pluginRegistry.findById(pluginId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Plugin not found: " + pluginId));

            PluginWrapper wrapper = plugin.getWrapper();
            if (wrapper == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Plugin wrapper is null for: " + pluginId);
            }

            PluginState currentState = wrapper.getPluginState();
            if (currentState == PluginState.STARTED) {
                logger.info("Plugin {} is already started", pluginId);
                return ResponseEntity.ok(PluginDTO.fromPlugin(plugin));
            }

            if (currentState != PluginState.STOPPED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot start plugin in state: " + currentState);
            }

            // Note: PF4J plugin manager should handle this
            // For now, just return the current plugin state
            logger.info("Plugin start requested for: {}", pluginId);

            return ResponseEntity.ok(PluginDTO.fromPlugin(plugin));
        });
    }

    /**
     * 停止插件
     */
    @PostMapping("/{pluginId}/stop")
    public Mono<ResponseEntity<PluginDTO>> stopPlugin(@PathVariable String pluginId) {
        return Mono.fromCallable(() -> {
            DatasourcePlugin plugin = pluginRegistry.findById(pluginId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Plugin not found: " + pluginId));

            PluginWrapper wrapper = plugin.getWrapper();
            if (wrapper == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Plugin wrapper is null for: " + pluginId);
            }

            PluginState currentState = wrapper.getPluginState();
            if (currentState == PluginState.STOPPED) {
                logger.info("Plugin {} is already stopped", pluginId);
                return ResponseEntity.ok(PluginDTO.fromPlugin(plugin));
            }

            if (currentState != PluginState.STARTED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot stop plugin in state: " + currentState);
            }

            // Note: PF4J plugin manager should handle this
            logger.info("Plugin stop requested for: {}", pluginId);

            return ResponseEntity.ok(PluginDTO.fromPlugin(plugin));
        });
    }

    /**
     * 卸载插件
     * <p>
     * 注意：需要先从 PF4J 插件管理器卸载，然后再从注册表注销
     */
    @DeleteMapping("/{pluginId}")
    public Mono<ResponseEntity<Map<String, String>>> unloadPlugin(@PathVariable String pluginId) {
        return Mono.fromCallable(() -> {
            if (!pluginRegistry.exists(pluginId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Plugin not found: " + pluginId);
            }

            // Unregister from registry
            boolean unregistered = pluginRegistry.unregister(pluginId);
            if (!unregistered) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to unregister plugin: " + pluginId);
            }

            logger.info("Plugin {} unloaded", pluginId);

            Map<String, String> result = Map.of(
                    "message", "Plugin unloaded successfully",
                    "pluginId", pluginId
            );

            return ResponseEntity.ok(result);
        });
    }

    /**
     * 重新加载插件
     */
    @PostMapping("/{pluginId}/reload")
    public Mono<ResponseEntity<PluginDTO>> reloadPlugin(@PathVariable String pluginId) {
        return Mono.fromCallable(() -> {
            DatasourcePlugin plugin = pluginRegistry.findById(pluginId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Plugin not found: " + pluginId));

            logger.info("Plugin reload requested for: {}", pluginId);

            // Unregister first
            pluginRegistry.unregister(pluginId);

            // Re-register (assuming the plugin is still available)
            pluginRegistry.register(plugin);

            return ResponseEntity.ok(PluginDTO.fromPlugin(plugin));
        });
    }
}