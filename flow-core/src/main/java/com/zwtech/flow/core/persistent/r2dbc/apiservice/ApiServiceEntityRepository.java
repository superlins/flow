package com.zwtech.flow.core.persistent.r2dbc.apiservice;

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
        @Query("SELECT * FROM flw_api_service WHERE datasource_key_ = :key AND datasource_version_ = :version")
        Flux<ApiServiceEntity> findByDatasourceKeyAndVersion(
                @Param("key") String key,
                @Param("version") Integer version);

        /**
         * 检查是否存在引用指定 Datasource 的 Service
         */
        @Query("SELECT COUNT(*) > 0 FROM flw_api_service WHERE datasource_key_ = :key AND datasource_version_ = :version")
        Mono<Boolean> existsByDatasourceKeyAndVersion(
                @Param("key") String key,
                @Param("version") Integer version);

        @Query("SELECT * FROM flw_api_service ORDER BY updated_at_ DESC")
        Flux<ApiServiceEntity> findAllOrdered();

        /**
         * 根据 key 删除 Service
         */
        Mono<Void> deleteByKey(String key);
}
