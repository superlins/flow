package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ApiService Repository 接口
 * 
 * 职责：提供领域模型的持久化抽象
 *
 * @author renc
 */
public interface ApiServiceRepository {

    /**
     * 根据 ServiceId 查找
     */
    Mono<ApiService> find(ServiceId id);

    /**
     * 查询所有 ApiService
     */
    Flux<ApiService> findAll();

    /**
     * 保存 ApiService
     */
    Mono<Void> save(ApiService service);

    /**
     * 根据 Datasource 引用查找所有 Service
     * 用于实现 DS-1 规则检查（isReferenced）
     */
    Flux<ApiService> findByDatasourceId(DatasourceId datasourceId);

    /**
     * 检查是否存在引用指定 Datasource 的 Service
     */
    Mono<Boolean> existsByDatasourceId(DatasourceId datasourceId);
}