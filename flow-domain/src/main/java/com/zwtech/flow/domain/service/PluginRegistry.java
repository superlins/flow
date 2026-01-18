package com.zwtech.flow.domain.service;

import com.zwtech.flow.domain.model.apidatasource.DatasourceType;
import com.zwtech.flow.domain.model.apidatasource.plugin.DatasourcePlugin;
import com.zwtech.flow.domain.model.apidatasource.plugin.DatasourcePluginMetadata;

import java.util.List;
import java.util.Optional;

/**
 * Plugin Registry 服务
 * <p>
 * 负责管理所有已加载的 DatasourcePlugin 插件，提供插件查询能力。
 * <p>
 * 主要职责:
 * - 维护已注册插件的生命周期
 * - 根据数据源类型查找对应的插件
 * - 提供插件列表和元数据查询
 * - 管理插件热加载/卸载状态
 *
 * @author renc
 */
public interface PluginRegistry {

    /**
     * 注册插件
     * <p>
     * 当插件被成功加载后调用此方法进行注册。
     * 会初始化插件并调用其 initialize() 方法。
     *
     * @param plugin 要注册的插件
     * @throws IllegalStateException 如果插件已注册
     */
    void register(DatasourcePlugin plugin);

    /**
     * 注销插件
     * <p>
     * 在插件卸载前调用此方法。
     * 会调用插件的 shutdown() 方法。
     *
     * @param pluginId 插件 ID
     * @return true 如果插件存在并被成功注销
     */
    boolean unregister(String pluginId);

    /**
     * 根据插件 ID 查找插件
     *
     * @param pluginId 插件 ID
     * @return Optional 插件实例，可能为空
     */
    Optional<DatasourcePlugin> findById(String pluginId);

    /**
     * 根据数据源类型查找对应的插件
     *
     * @param type 数据源类型
     * @return Optional 插件实例，可能为空
     */
    Optional<DatasourcePlugin> findByType(DatasourceType type);

    /**
     * 获取所有已注册的插件
     *
     * @return 插件列表
     */
    List<DatasourcePlugin> listPlugins();

    /**
     * 获取所有插件的元数据
     *
     * @return 插件元数据列表
     */
    List<DatasourcePluginMetadata> listMetadata();

    /**
     * 检查指定类型的插件是否已就绪
     *
     * @param type 数据源类型
     * @return true 如果对应插件存在且已就绪
     */
    boolean isTypeReady(DatasourceType type);

    /**
     * 检查 pluginId 是否存在
     *
     * @param pluginId 插件 ID
     * @return true 如果插件已注册
     */
    boolean exists(String pluginId);

    /**
     * 清空所有插件（用于测试）
     */
    void clear();
}