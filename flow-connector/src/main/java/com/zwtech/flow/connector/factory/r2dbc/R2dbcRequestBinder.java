package com.zwtech.flow.connector.factory.r2dbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zwtech.flow.connector.binding.RequestBinder;
import com.zwtech.flow.connector.specs.R2dbcDatasourceSpecs;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.core.VariableContext;
import com.zwtech.flow.core.parser.TemplateExpressionParser;
import com.zwtech.flow.domain.model.apidatasource.operation.SqlDatasourceOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * R2DBC 请求绑定器
 * <p>
 * 将 ExecutionExchange 转换为 R2dbcRequestSpec。
 * 使用 VariableContext 解析模板表达式。
 *
 * @author renc
 */
public final class R2dbcRequestBinder
        implements RequestBinder<R2dbcRequestSpec, R2dbcDatasourceSpecs> {

    private static final TemplateExpressionParser TEMPLATE_PARSER = new TemplateExpressionParser();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public R2dbcRequestBinder() {
    }

    @Override
    public R2dbcRequestSpec bind(ExecutionExchange exchange, R2dbcDatasourceSpecs specs) {
        SqlDatasourceOperation operation = specs.getOperation();
        VariableContext variableContext = exchange.getVariableContext();

        // 解析 SQL（支持模板表达式）
        String sql = parseSql(operation.sql(), variableContext);

        // 解析 SQL 参数（JSON 模板字符串）
        JsonNode parameters = parseJsonParameters(operation.paramsTemplate(), variableContext);

        return R2dbcRequestSpec.builder()
                .sql(sql)
                .parameters(parameters)
                .build();
    }

    /**
     * 解析 SQL，支持模板表达式
     */
    private String parseSql(String sqlTemplate, VariableContext variableContext) {
        if (sqlTemplate == null || sqlTemplate.isEmpty()) {
            throw new IllegalArgumentException("SQL cannot be null or empty");
        }

        Object parsed = TEMPLATE_PARSER.parseTemplate(sqlTemplate, variableContext);
        return parsed != null ? String.valueOf(parsed) : sqlTemplate;
    }

    /**
     * 解析 SQL 参数（JSON 模板字符串）
     * 例如："{\"userId\":\"{{ #dsInput.userId }}\",\"limit\":\"{{ #dsInput.limit }}\"}"
     * 转换为 JsonNode 数组格式，方便 DatabaseClient 绑定
     */
    private JsonNode parseJsonParameters(String paramsTemplate, VariableContext variableContext) {
        if (paramsTemplate == null || paramsTemplate.isEmpty()) {
            return OBJECT_MAPPER.createArrayNode();
        }

        // 解析模板替换表达式
        String resolved = parseStringTemplate(paramsTemplate, variableContext);

        try {
            // 将 JSON 字符串解析为 Map
            Map<String, Object> parametersMap = OBJECT_MAPPER.readValue(
                    resolved, new TypeReference<Map<String, Object>>() {});

            // 转换为数组格式，按键排序以确保顺序一致性
            List<Object> parameters = new ArrayList<>();
            parametersMap.keySet().stream()
                    .sorted()
                    .forEach(key -> {
                        Object value = parametersMap.get(key);
                        parameters.add(value);
                    });

            return OBJECT_MAPPER.valueToTree(parameters);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse SQL parameters template: " + paramsTemplate, e);
        }
    }

    /**
     * 解析字符串模板
     */
    private String parseStringTemplate(String template, VariableContext variableContext) {
        Object parsed = TEMPLATE_PARSER.parseTemplate(template, variableContext);
        return parsed != null ? String.valueOf(parsed) : template;
    }
}
