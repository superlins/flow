package com.zwtech.flow.domain.model.workflow;

import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Workflow 仓储接口
 *
 * @author renc
 */
public interface WorkflowRepository {

    /**
     * 保存 Workflow
     */
    Mono<Workflow> save(Workflow workflow);

    /**
     * 根据 key 和 version 查找 Workflow
     */
    Mono<Workflow> findById(String key, int version);

    /**
     * 根据 key 查找所有版本的 Workflow
     */
    Mono<List<Workflow>> findByKey(String key);

    /**
     * 按 key 和 status 查询 Workflow 列表
     */
    Mono<List<Workflow>> findByKey(String key, WorkflowStatus status);

    /**
     * 删除 Workflow（仅支持归档状态）
     */
    Mono<Void> delete(String key, int version);
}
