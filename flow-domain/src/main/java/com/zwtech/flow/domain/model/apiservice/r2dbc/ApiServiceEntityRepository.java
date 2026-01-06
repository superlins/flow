package com.zwtech.flow.domain.model.apiservice.r2dbc;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ApiService 实体 Repository
 * 
 * @author renc
 */
interface ApiServiceEntityRepository extends ReactiveCrudRepository<ApiServiceEntity, Long> {

    /**
     * 根据 ServiceId 查找
     */
    Mono<ApiServiceEntity> findByKey(String key);

    /**
     * 根据 Datasource 引用查找所有 Service
     * 用于实现 isReferenced 检查
     */
    @Query("SELECT * FROM FLW_API_SERVICE WHERE DATASOURCE_KEY_ = :key AND DATASOURCE_VERSION_ = :version")
    Flux<ApiServiceEntity> findByDatasourceKeyAndVersion(
            @Param("key") String key,
            @Param("version") Integer version);

    /**
     * 检查是否存在引用指定 Datasource 的 Service
     */
    @Query("SELECT COUNT(*) > 0 FROM FLW_API_SERVICE WHERE DATASOURCE_KEY_ = :key AND DATASOURCE_VERSION_ = :version")
    Mono<Boolean> existsByDatasourceKeyAndVersion(
            @Param("key") String key,
            @Param("version") Integer version);
}

