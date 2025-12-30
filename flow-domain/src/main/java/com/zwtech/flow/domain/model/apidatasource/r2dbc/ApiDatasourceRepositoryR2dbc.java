package com.zwtech.flow.domain.model.apidatasource.r2dbc;

import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasourceRepository;
import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import reactor.core.publisher.Mono;

/**
 * @author renc
 */
public class ApiDatasourceRepositoryR2dbc implements ApiDatasourceRepository {

    @Override
    public Mono<ApiDatasource> find(DatasourceId id) {
        return null;
    }

    @Override
    public Mono<Void> save(ApiDatasource datasource) {
        return null;
    }
}
