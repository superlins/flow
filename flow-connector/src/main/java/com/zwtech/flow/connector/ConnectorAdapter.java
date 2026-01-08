package com.zwtech.flow.connector;

import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import reactor.core.publisher.Mono;

/**
 * 连接器适配器：负责将 ExecutionExchange 转换为具体 RequestSpec/ResponseSpec 执行流程。
 *
 * @author renc
 */
public interface ConnectorAdapter {

    /**
     * 执行数据源操作
     * 
     * @param exchange ExecutionExchange
     * @param datasource ApiDatasource
     * @param operationKey Operation 的 key（Rule 3：ApiService 必须显式绑定 operationKey）
     * @return 执行结果
     */
    Mono<ExecutionExchange> execute(ExecutionExchange exchange, ApiDatasource datasource, String operationKey);
}