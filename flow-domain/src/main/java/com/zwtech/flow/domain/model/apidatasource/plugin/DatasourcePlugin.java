package com.zwtech.flow.domain.model.apidatasource.plugin;

import com.zwtech.flow.domain.model.apidatasource.DatasourceType;
import org.pf4j.PluginWrapper;

/**
 * DatasourcePlugin 插件接口
 * <p>
 * 定义数据源插件的基本契约，支持插件系统扩展
 *
 * @author renc
 */
public interface DatasourcePlugin {

    /**
     * 获取插件 ID
     */
    String getPluginId();

    /**
     * 获取插件名称
     */
    String getPluginName();

    /**
     * 获取插件版本
     */
    default String getVersion() {
        return "1.0.0";
    }

    /**
     * 获取插件描述
     */
    default String getDescription() {
        return "";
    }

    /**
     * 获取支持的数据源类型
     */
    DatasourceType getSupportedType();

    /**
     * 检查插件是否支持指定的数据源类型
     */
    default boolean supports(DatasourceType type) {
        return getSupportedType() == type;
    }

    /**
     * 获取插件元数据
     */
    default DatasourcePluginMetadata getMetadata() {
        return DatasourcePluginMetadata.builder()
                .id(getPluginId())
                .name(getPluginName())
                .version(getVersion())
                .description(getDescription())
                .supportedTypes(java.util.List.of(getSupportedType()))
                .build();
    }

    /**
     * 获取 PF4J PluginWrapper（用于访问运行时状态）
     */
    PluginWrapper getWrapper();

    /**
     * 检查插件是否已就绪
     */
    default <T extends DatasourcePlugin> boolean isReady(T plugin) {
        PluginWrapper wrapper = plugin.getWrapper();
        return wrapper != null && wrapper.getPluginState() == org.pf4j.PluginState.STARTED;
    }

    /**
     * 检查插件是否就绪
     */
    default boolean isReady() {
        PluginWrapper wrapper = getWrapper();
        return wrapper != null && wrapper.getPluginState() == org.pf4j.PluginState.STARTED;
    }

    /**
     * 初始化插件
     */
    default void initialize() {
    }

    /**
     * 关闭插件
     */
    default void shutdown() {
    }
}