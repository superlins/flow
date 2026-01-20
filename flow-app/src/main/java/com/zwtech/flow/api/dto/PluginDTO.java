package com.zwtech.flow.api.dto;

import com.zwtech.flow.domain.model.apidatasource.plugin.DatasourcePlugin;
import com.zwtech.flow.domain.model.apidatasource.plugin.DatasourcePluginMetadata;
import com.zwtech.flow.domain.model.plugin.Plugin;
import com.zwtech.flow.domain.model.plugin.PluginContent;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

import java.time.Instant;
import java.util.Map;

/**
 * Plugin DTO
 * <p>
 * 用于 API 响应，包含插件的完整信息
 *
 * @author renc
 */
public record PluginDTO(
        String id,
        String name,
        String version,
        String description,
        String type,
        String status,
        Map<String, Object> content,
        Map<String, Object> config,
        Instant createdAt,
        Instant updatedAt,
        Instant enabledAt,
        Instant disabledAt,
        // 基础设施层状态（来自 SpringPluginManager，只读显示）
        String runtimeStatus,
        String runtimePluginId
) {

    /**
     * 从领域模型创建 DTO
     */
    public static PluginDTO fromDomain(Plugin plugin) {
        // 内容转换
        Map<String, Object> contentMap = switch (plugin.content()) {
            case PluginContent.JarContent jar -> Map.of(
                    "jarPath", jar.jarPath(),
                    "pluginClassName", jar.pluginClassName()
            );
            case PluginContent.ScriptContent script -> Map.of(
                    "language", script.language().getDisplayName(),
                    "scriptBody", script.scriptBody()
            );
            case PluginContent.InlineContent inline -> Map.of("code", inline.code(), "language", inline.language());
        };

        return new PluginDTO(
                plugin.id().value(),
                plugin.name(),
                plugin.version(),
                plugin.description(),
                plugin.type().getCode(),
                plugin.status().getCode(),
                contentMap,
                plugin.config(),
                plugin.createdAt(),
                plugin.updatedAt(),
                plugin.enabledAt(),
                plugin.disabledAt(),
                null, // runtimeStatus - 由事件订阅者填充
                null  // runtimePluginId - 由事件订阅者填充
        );
    }

    /**
     * 从领域模型和 PF4J PluginWrapper 创建 DTO（包含运行时状态）
     */
    public static PluginDTO fromDomainWithRuntime(Plugin plugin, PluginWrapper wrapper) {
        PluginDTO dto = fromDomain(plugin);

        if (wrapper != null) {
            dto = new PluginDTO(
                    dto.id(),
                    dto.name(),
                    dto.version(),
                    dto.description(),
                    dto.type(),
                    dto.status(),
                    dto.content(),
                    dto.config(),
                    dto.createdAt(),
                    dto.updatedAt(),
                    dto.enabledAt(),
                    dto.disabledAt(),
                    wrapper.getPluginState().name(),
                    wrapper.getPluginId()
            );
        }

        return dto;
    }

    /**
     * 从 DatasourcePluginMetadata 创建 DTO
     */
    public static PluginDTO fromMetadata(DatasourcePluginMetadata metadata) {
        return new PluginDTO(
                metadata.pluginId(),
                metadata.pluginName(),
                metadata.version(),
                metadata.description(),
                null, // type
                null, // status
                null, // content
                null, // config
                null, // createdAt
                null, // updatedAt
                null, // enabledAt
                null, // disabledAt
                null, // runtimeStatus
                null  // runtimePluginId
        );
    }

    /**
     * 从 DatasourcePlugin 创建 DTO
     */
    public static PluginDTO fromPlugin(DatasourcePlugin plugin) {
        PluginWrapper wrapper = plugin.getWrapper();
        String runtimeStatus = wrapper != null ? wrapper.getPluginState().name() : null;
        String runtimePluginId = wrapper != null ? wrapper.getPluginId() : null;

        return new PluginDTO(
                plugin.getPluginId(),
                plugin.getPluginName(),
                null, // version
                null, // description
                null, // type
                runtimeStatus,
                null, // content
                null, // config
                null, // createdAt
                null, // updatedAt
                null, // enabledAt
                null, // disabledAt
                runtimeStatus,
                runtimePluginId
        );
    }
}