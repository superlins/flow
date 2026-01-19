package com.zwtech.flow.connector.factory.r2dbc;

import com.zwtech.flow.connector.Connector;
import com.zwtech.flow.connector.ExecutionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * R2DBC Connector
 * <p>
 * 负责执行 R2DBC 数据库操作。
 * 支持参数化 SQL 查询和响应转换。
 *
 * @author renc
 */
public class R2dbcConnector implements Connector<R2dbcRequestSpec, R2dbcResponseSpec> {

    private static final Logger LOGGER = LoggerFactory.getLogger(R2dbcConnector.class);
    private final DatabaseClient databaseClient;

    public R2dbcConnector(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<R2dbcResponseSpec> connect(R2dbcRequestSpec spec, ExecutionAttributes attributes) {
        var exec = databaseClient.sql(spec.getSql());

        // 绑定 SQL 参数
        List<Object> parameters = spec.getParametersAsList();
        for (int i = 0; i < parameters.size(); i++) {
            exec = exec.bind(i, parameters.get(i));
        }

        LOGGER.debug("Executing R2DBC query: {} with {} parameters", spec.getSql(), parameters.size());

        return exec.fetch()
                .all()
                .collectList()
                .map(rows -> {
                    // 将查询结果转换为 List<JsonNode>
                    var jsonRows = convertRowsToJson(rows);
                    LOGGER.debug("R2DBC query returned {} rows", jsonRows.size());
                    return R2dbcResponseSpec.builder().rows(jsonRows).build();
                })
                .onErrorMap(ex -> {
                    LOGGER.error("R2DBC query execution failed: {}", ex.getMessage(), ex);
                    return new RuntimeException("R2DBC query execution failed: " + ex.getMessage(), ex);
                });
    }

    /**
     * 将数据库查询结果转换为 JsonNode 列表
     */
    private List<JsonNode> convertRowsToJson(List<Map<String, Object>> rows) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(rows, new TypeReference<List<JsonNode>>() {});
    }
}
