package com.zwtech.flow.domain.service;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import reactor.core.publisher.Mono;

/**
 * ApiDatasource 使用情况领域服务
 * 
 * 职责：判断 Datasource 是否正在被使用
 * 用于实现 DS-1 规则检查
 *
 * @author renc
 */
public interface ApiDatasourceUsageService {

    /**
     * 判断一个 ApiDatasource 是否正在被使用
     * 
     * @param datasourceId Datasource 标识
     * @return true 如果正在被 ApiService 引用
     */
    Mono<Boolean> isDatasourceInUse(DatasourceId datasourceId);
}