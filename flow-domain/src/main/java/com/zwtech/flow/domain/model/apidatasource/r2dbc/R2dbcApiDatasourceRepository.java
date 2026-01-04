package com.zwtech.flow.domain.model.apidatasource.r2dbc;

import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasourceRepository;
import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author renc
 */
@Repository
public class R2dbcApiDatasourceRepository implements ApiDatasourceRepository {

    private final ApiDatasourceEntityRepository apiDatasourceEntityRepository;

    public R2dbcApiDatasourceRepository(ApiDatasourceEntityRepository apiDatasourceEntityRepository) {
        this.apiDatasourceEntityRepository = apiDatasourceEntityRepository;
    }

    @Override
    public Mono<ApiDatasource> findById(DatasourceId id) {
        return apiDatasourceEntityRepository.findByKeyAndVersion(id.key(), id.version())
                .map(ApiDatasourceEntity::toApiDatasource);
    }

    @Override
    public Flux<ApiDatasource> findByKey(String key) {
        return apiDatasourceEntityRepository.findByKey(key)
                .map(ApiDatasourceEntity::toApiDatasource);
    }

    @Override
    public Mono<ApiDatasource> save(ApiDatasource ds) {
        return apiDatasourceEntityRepository.save(ApiDatasourceEntity.fromApiDatasource(ds))
                .map(ApiDatasourceEntity::toApiDatasource);
    }

    @Override
    public Mono<Boolean> isReferenced(DatasourceId id) {
        return apiDatasourceEntityRepository.isReferenced(id.key(), id.version());
    }

}
