package com.zwtech.flow.domain.model.apidatasource;

import reactor.core.publisher.Mono;

/**
 * @author renc
 */
public interface ApiDatasourceRepository {

    Mono<ApiDatasource> find(DatasourceId id);

    Mono<Void> save(ApiDatasource datasource);
}
