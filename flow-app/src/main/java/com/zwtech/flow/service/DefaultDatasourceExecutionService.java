package com.zwtech.flow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zwtech.flow.connector.ConnectorAdapter;
import com.zwtech.flow.core.DefaultExecutionExchange;
import com.zwtech.flow.core.DefaultVariableContext;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasourceRepository;
import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import com.zwtech.flow.domain.service.DatasourceExecutionService;
import com.zwtech.flow.domain.service.SchemaValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

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
    private final List<ConnectorAdapter> connectorAdapters;
    private final SchemaValidationService schemaValidationService;

    public DefaultDatasourceExecutionService(
            ApiDatasourceRepository datasourceRepository,
            List<ConnectorAdapter> connectorAdapters,
            SchemaValidationService schemaValidationService) {
        this.datasourceRepository = datasourceRepository;
        this.connectorAdapters = connectorAdapters;
        this.schemaValidationService = schemaValidationService;
    }

    @Override
    public Mono<JsonNode> execute(DatasourceId datasourceId, JsonNode input) {
        logger.info("Executing datasource: {}", datasourceId);

        return datasourceRepository.findById(datasourceId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Datasource not found: " + datasourceId)))
                .flatMap(datasource -> {
                    var contract = datasource.contract();
                    // 实际应用中可以添加额外验证逻辑
                    schemaValidationService.validate(contract.inputSchema(), input);

                    // 获取对应的 ConnectorAdapter
                    ConnectorAdapter adapter = getConnectorAdapter(datasource);

                    // 创建 Exchange（添加空的 response 用于 VariableContext）
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode emptyResponse = mapper.createObjectNode();
                    var variableContext = new DefaultVariableContext(input, emptyResponse);
                    ExecutionExchange exchange = new DefaultExecutionExchange(input, emptyResponse, variableContext);

                    // 执行操作
                    return adapter.execute(exchange, datasource)
                            .map(ExecutionExchange::getResponse)
                            .doOnNext(response -> {
                                schemaValidationService.validate(contract.outputSchema(), response);
                            });
                });
    }

    /**
     * 根据 datasource 类型获取对应的 ConnectorAdapter
     */
    private ConnectorAdapter getConnectorAdapter(ApiDatasource datasource) {
        String type = datasource.type().name();
        return connectorAdapters.stream()
                .filter(c -> c.supports(type))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Datasource type not supported: " + type));
    }
}
