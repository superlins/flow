package com.zwtech.flow.domain.service;

import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.model.apidatasource.DatasourceContract;
import com.fasterxml.jackson.databind.JsonNode;
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
     * @param operationKey 操作标识
     * @param input 输入数据
     * @param contract Datasource 契约（用于验证）
     * @return 输出数据
     */
    Mono<JsonNode> execute(DatasourceId datasourceId, String operationKey, JsonNode input, DatasourceContract contract);
}
