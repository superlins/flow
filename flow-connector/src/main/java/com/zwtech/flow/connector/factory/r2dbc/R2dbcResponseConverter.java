package com.zwtech.flow.connector.factory.r2dbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zwtech.flow.connector.binding.ResponseConverter;
import com.zwtech.flow.connector.specs.DatasourceSpecs;
import com.zwtech.flow.connector.specs.R2dbcDatasourceSpecs;
import com.zwtech.flow.core.VariableContext;
import com.zwtech.flow.core.parser.spel.ExpressionContextParser;
import com.zwtech.flow.domain.model.apidatasource.operation.SqlDatasourceOperation;

import java.util.List;
import java.util.Map;

/**
 * R2DBC 响应转换器
 * <p>
 * 将 R2dbcResponseSpec 转换为 JsonNode，支持字段映射和 SpEL 表达式解析。
 *
 * @author renc
 */
public final class R2dbcResponseConverter
        implements ResponseConverter<R2dbcResponseSpec, R2dbcDatasourceSpecs> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ExpressionContextParser SPEL_PARSER = new ExpressionContextParser();

    public R2dbcResponseConverter() {
    }

    @Override
    public JsonNode convert(R2dbcResponseSpec response) {
        // 直接返回行数据
        if (response.getRows() == null || response.getRows().isEmpty()) {
            return OBJECT_MAPPER.createArrayNode();
        }
        return OBJECT_MAPPER.valueToTree(response.getRows());
    }

    @Override
    public JsonNode project(R2dbcResponseSpec response, DatasourceSpecs specs, VariableContext context) {
        // 转换到具体类型
        var r2dbcSpecs = (R2dbcDatasourceSpecs) specs;
        return project(response, r2dbcSpecs.getOperation(), context);
    }

    /**
     * 投影方法，使用 SqlDatasourceOperation 的 responseBodyTemplate
     */
    public JsonNode project(R2dbcResponseSpec response, SqlDatasourceOperation operation, VariableContext context) {
        String responseBodyTemplate = operation.responseBodyTemplate();
        List<JsonNode> rows = response.getRows();

        // 如果没有映射配置，直接返回所有行
        if (responseBodyTemplate == null || responseBodyTemplate.isEmpty()) {
            return convert(response);
        }

        // 解析 JSON 模板字符串，提取映射
        try {
            JsonNode templateNode = OBJECT_MAPPER.readTree(responseBodyTemplate);
            Map<String, String> outputMappings = OBJECT_MAPPER.convertValue(
                    templateNode, new TypeReference<Map<String, String>>() {});

            // 如果只有一行数据，返回映射后的对象
            if (rows.size() == 1) {
                return projectSingleRow(rows.get(0), outputMappings, context);
            }

            // 如果有多行数据，返回映射后的数组
            ArrayNode resultArray = OBJECT_MAPPER.createArrayNode();
            for (JsonNode row : rows) {
                resultArray.add(projectSingleRow(row, outputMappings, context));
            }
            return resultArray;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse responseBodyTemplate: " + responseBodyTemplate, e);
        }
    }

    @Override
    public JsonNode toVariableFormat(R2dbcResponseSpec response) {
        ObjectNode responseNode = OBJECT_MAPPER.createObjectNode();

        if (response.getRows() != null) {
            responseNode.set("rows", OBJECT_MAPPER.valueToTree(response.getRows()));
            responseNode.put("rowCount", response.getRows().size());
        }

        return responseNode;
    }

    /**
     * 映射单行数据
     */
    private JsonNode projectSingleRow(JsonNode row, Map<String, String> outputMappings, VariableContext context) {
        ObjectNode outputNode = OBJECT_MAPPER.createObjectNode();

        outputMappings.forEach((outputKey, expression) -> {
            if (expression != null) {
                // 使用 rootObject 方式传递 row 变量
                Map<String, Object> rootMap = new java.util.HashMap<>();
                rootMap.put("row", row);
                // 添加所有上下文变量
                context.getVariables().forEach(rootMap::put);

                Object value = evaluateSpel(expression, rootMap);
                if (value instanceof JsonNode) {
                    outputNode.set(outputKey, (JsonNode) value);
                } else if (value != null) {
                    outputNode.putPOJO(outputKey, value);
                }
            }
        });

        return outputNode;
    }

    /**
     * 使用 SpEL 解析器评估表达式
     */
    private Object evaluateSpel(String expression, Map<String, Object> rootMap) {
        return SPEL_PARSER.parseValue(expression, rootMap);
    }
}