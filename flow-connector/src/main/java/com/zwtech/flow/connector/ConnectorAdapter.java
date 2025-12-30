package com.zwtech.flow.connector;

import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import reactor.core.publisher.Mono;

public interface ConnectorAdapter {

    Mono<ExecutionExchange> execute(ExecutionExchange exchange, ApiDatasource datasource);
}