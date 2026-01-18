package com.zwtech.flow.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import reactor.core.publisher.Mono;

/**
 * Datasource 执行领域服务
 * 负责执行 Datasource 的操作
 *
 * @author renc
 */
public interface DatasourceExecutionService {

    /**
     * 执行 Datasource 操作
     *
     * @param datasourceId Datasource 标识
     * @param input 输入数据
     * @return 输出数据
     */
    Mono<JsonNode> execute(DatasourceId datasourceId, JsonNode input);
}
