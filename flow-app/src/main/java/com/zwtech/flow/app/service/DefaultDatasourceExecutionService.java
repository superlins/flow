package com.zwtech.flow.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.zwtech.flow.connector.ConnectorAdapter;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.core.DefaultExecutionExchange;
import com.zwtech.flow.core.plugin.SpringPluginManager;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.DatasourceContract;
import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasourceRepository;
import com.zwtech.flow.domain.service.DatasourceExecutionService;
import com.zwtech.flow.domain.service.SchemaValidationService;
import com.zwtech.flow.connector.factory.http.HttpConnectorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Datasource 执行服务默认实现（应用层）
 * 负责协调 Repository 和 ConnectorAdapter 来执行 Datasource 操作
 *
 * @author renc
 */
@Service
public class DefaultDatasourceExecutionService implements DatasourceExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDatasourceExecutionService.class);

    private final ApiDatasourceRepository datasourceRepository;
    private final SpringPluginManager pluginManager;
    private final HttpConnectorAdapter httpConnectorAdapter;
    private final SchemaValidationService schemaValidationService;

    public DefaultDatasourceExecutionService(
            ApiDatasourceRepository datasourceRepository,
            SpringPluginManager pluginManager,
            HttpConnectorAdapter httpConnectorAdapter,
            SchemaValidationService schemaValidationService) {
        this.datasourceRepository = datasourceRepository;
        this.pluginManager = pluginManager;
        this.httpConnectorAdapter = httpConnectorAdapter;
        this.schemaValidationService = schemaValidationService;
    }

    @Override
    public Mono<JsonNode> execute(DatasourceId datasourceId, String operationKey, JsonNode input, DatasourceContract contract) {
        logger.info("Executing datasource: {}", datasourceId);

        return datasourceRepository.findById(datasourceId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Datasource not found: " + datasourceId)))
                .flatMap(datasource -> {
                    // 对 operationKey 做简单验证（因为 DatasourceOperation 接口没有 key() 方法）
                    // 实际应用中可以添加额外验证逻辑

                    // 获取对应的 ConnectorAdapter
                    ConnectorAdapter adapter = getConnectorAdapter(datasource);
                    if (adapter == null) {
                        return Mono.error(new UnsupportedOperationException(
                                "Unsupported datasource type: " + datasource.type()));
                    }

                    // 创建 Exchange（添加空的 response 用于 VariableContext）
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    JsonNode emptyResponse = mapper.createObjectNode();
                    var variableContext = new com.zwtech.flow.core.DefaultVariableContext(input, emptyResponse);
                    ExecutionExchange exchange = new DefaultExecutionExchange(input, emptyResponse, variableContext);

                    // 执行操作
                    return adapter.execute(exchange, datasource)
                            .map(ExecutionExchange::getResponse);
                });
    }

    /**
     * 根据 datasource 类型获取对应的 ConnectorAdapter
     */
    private ConnectorAdapter getConnectorAdapter(ApiDatasource datasource) {
        String type = datasource.type().name();

        // 目前支持 HTTP 类型
        if (httpConnectorAdapter != null && httpConnectorAdapter.supports(type)) {
            return httpConnectorAdapter;
        }

        // 可以在这里添加其他类型的 adapter
        // 例如：R2DBC, Cassandra 等
        logger.warn("No adapter found for datasource type: {}", type);
        return null;
    }
}
