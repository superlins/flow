package com.zwtech.flow.domain.service;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import reactor.core.publisher.Mono;

public interface ApiDatasourceUsageService {

    // 判断一个 ApiDatasource 是否正在被使用
    Mono<Boolean> isDatasourceInUse(DatasourceId datasourceId);
}