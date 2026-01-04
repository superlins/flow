package com.zwtech.flow.domain.model.apidatasource.r2dbc;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

interface ApiDatasourceEntityRepository extends ReactiveCrudRepository<ApiDatasourceEntity, Long> {

    Flux<ApiDatasourceEntity> findByKey(String key);

    @Query("SELECT * FROM ApiDatasourceEntity d WHERE d.key = :key ORDER BY d.version DESC LIMIT 1")
    Mono<ApiDatasourceEntity> findLatestByKey(@Param("key") String key);

    Mono<ApiDatasourceEntity> findByKeyAndVersion(String key, int version);

    @Query("SELECT EXISTS(SELECT * FROM ApiDatasourceEntity d WHERE d.key = :key AND d.version = :version)")
    Mono<Boolean> isReferenced(String key, int version);
}