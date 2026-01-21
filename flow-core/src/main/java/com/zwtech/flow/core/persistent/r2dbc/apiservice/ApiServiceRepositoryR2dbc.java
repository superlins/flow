package com.zwtech.flow.core.persistent.r2dbc.apiservice;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.model.apiservice.ApiService;
import com.zwtech.flow.domain.model.apiservice.ApiServiceRepository;
import com.zwtech.flow.domain.model.apiservice.ServiceId;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ApiService R2DBC Repository 实现
 * 
 * 职责：在领域模型与数据库模型之间翻译语义
 *
 * @author renc
 */
@Repository
public class ApiServiceRepositoryR2dbc implements ApiServiceRepository {

    private final ApiServiceEntityRepository apiServiceEntityRepository;

    public ApiServiceRepositoryR2dbc(ApiServiceEntityRepository apiServiceEntityRepository) {
        this.apiServiceEntityRepository = apiServiceEntityRepository;
    }

    @Override
    public Mono<ApiService> find(ServiceId id) {
        return apiServiceEntityRepository.findByKey(id.value())
                .map(ApiServiceEntity::toApiService);
    }

    @Override
    public Flux<ApiService> findAll() {
        return apiServiceEntityRepository.findAllOrdered()
                .map(ApiServiceEntity::toApiService);
    }

    @Override
    public Mono<Void> save(ApiService service) {
        return apiServiceEntityRepository.save(ApiServiceEntity.fromApiService(service))
                .then();
    }

    @Override
    public Flux<ApiService> findByDatasourceId(DatasourceId datasourceId) {
        return apiServiceEntityRepository.findByDatasourceKeyAndVersion(
                datasourceId.key(),
                datasourceId.version())
                .map(ApiServiceEntity::toApiService);
    }

    @Override
    public Mono<Boolean> existsByDatasourceId(DatasourceId datasourceId) {
        return apiServiceEntityRepository.existsByDatasourceKeyAndVersion(
                datasourceId.key(),
                datasourceId.version());
    }

    @Override
    public Mono<Void> delete(ServiceId id) {
        return apiServiceEntityRepository.deleteByKey(id.value());
    }
}
