package com.zwtech.flow.domain.model.apidatasource.r2dbc;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author renc
 */
interface ApiDatasourceEntityRepository extends ReactiveCrudRepository<ApiDatasourceEntity, Long> {

    Flux<ApiDatasourceEntity> findByKey(String key);

    @Query("SELECT * FROM flw_api_datasource d WHERE d.key_ = :key ORDER BY d.version_ DESC LIMIT 1")
    Mono<ApiDatasourceEntity> findLatestByKey(@Param("key") String key);

    Mono<ApiDatasourceEntity> findByKeyAndVersion(String key, int version);

    @Query("SELECT * FROM flw_api_datasource ORDER BY updated_at_ DESC")
    Flux<ApiDatasourceEntity> findAllOrdered();

    /**
     * 根据 key 和 version 删除 Datasource
     */
    Mono<Void> deleteByKeyAndVersion(String key, int version);
}