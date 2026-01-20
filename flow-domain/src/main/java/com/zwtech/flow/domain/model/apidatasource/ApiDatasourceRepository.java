package com.zwtech.flow.domain.model.apidatasource;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author renc
 */
public interface ApiDatasourceRepository {

    Mono<ApiDatasource> findById(DatasourceId id);

    Flux<ApiDatasource> findByKey(String key);

    Flux<ApiDatasource> findAll();

    Mono<ApiDatasource> save(ApiDatasource datasource);

    Mono<Boolean> isReferenced(DatasourceId id);

    /**
     * 删除数据源
     */
    default Mono<Void> delete(DatasourceId id) {
        throw new UnsupportedOperationException("Delete operation is not supported (DS-4 rule)");
    }
}