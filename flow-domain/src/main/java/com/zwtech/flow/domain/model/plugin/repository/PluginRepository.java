package com.zwtech.flow.domain.model.plugin.repository;

import com.zwtech.flow.domain.model.plugin.Plugin;
import com.zwtech.flow.domain.model.plugin.PluginId;

import java.util.List;
import java.util.Optional;

/**
 * Plugin 仓储接口
 * <p>
 * 负责持久化插件元数据（领域模型），不涉及插件生命周期管理。
 * 真正的插件加载/启动/停止由基础设施层的 SpringPluginManager 负责。
 *
 * @author renc
 */
public interface PluginRepository {

    /**
     * 保存或更新插件
     */
    Plugin save(Plugin plugin);

    /**
     * 根据 ID 查找插件
     */
    Optional<Plugin> findById(PluginId id);

    /**
     * 查找所有插件
     */
    List<Plugin> findAll();

    /**
     * 根据状态查找插件
     */
    List<Plugin> findByStatus(PluginStatus status);

    /**
     * 删除插件
     */
    void delete(PluginId id);

    /**
     * 根据类型统计插件数量
     */
    long countByType(String pluginTypeCode);
}
