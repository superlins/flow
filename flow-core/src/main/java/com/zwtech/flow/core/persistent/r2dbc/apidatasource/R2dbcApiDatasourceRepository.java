package com.zwtech.flow.core.persistent.r2dbc.apidatasource;

import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasourceRepository;
import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.model.apiservice.ApiServiceRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ApiDatasource R2DBC Repository 实现
 *
 * 职责：在领域模型与数据库模型之间翻译语义
 *
 * @author renc
 */
@Repository
public class R2dbcApiDatasourceRepository implements ApiDatasourceRepository {

    private final ApiDatasourceEntityRepository apiDatasourceEntityRepository;
    private final ApiServiceRepository apiServiceRepository;

    public R2dbcApiDatasourceRepository(
            ApiDatasourceEntityRepository apiDatasourceEntityRepository,
            ApiServiceRepository apiServiceRepository) {
        this.apiDatasourceEntityRepository = apiDatasourceEntityRepository;
        this.apiServiceRepository = apiServiceRepository;
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
    public Flux<ApiDatasource> findAll() {
        return apiDatasourceEntityRepository.findAllOrdered()
                .map(ApiDatasourceEntity::toApiDatasource);
    }

    @Override
    public Mono<ApiDatasource> save(ApiDatasource ds) {
        return apiDatasourceEntityRepository.save(ApiDatasourceEntity.fromApiDatasource(ds))
                .map(ApiDatasourceEntity::toApiDatasource);
    }

    @Override
    public Mono<Void> delete(DatasourceId id) {
        return apiDatasourceEntityRepository.deleteByKeyAndVersion(id.key(), id.version());
    }

    /**
     * 检查 Datasource 是否被 ApiService 引用
     *
     * 实现 DS-1 规则：被引用的 Datasource 不可修改核心字段
     *
     * 通过查询 ApiServiceRepository 来检查引用关系
     */
    @Override
    public Mono<Boolean> isReferenced(DatasourceId id) {
        return apiServiceRepository.existsByDatasourceId(id);
    }

}
