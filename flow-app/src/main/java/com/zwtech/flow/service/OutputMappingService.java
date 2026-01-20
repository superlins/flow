package com.zwtech.flow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zwtech.flow.core.parser.spel.EmbeddedContextExpressionParser;
import com.zwtech.flow.core.parser.spel.ExpressionContextParser;
import com.zwtech.flow.domain.model.apiservice.FieldBinding;
import com.zwtech.flow.domain.model.apiservice.ServiceMapping;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * OutputMappingService
 *
 * 负责将 Datasource 的输出映射到 ApiService 的输出
 *
 * 执行流程：
 * 1. 创建变量上下文（包含 #serviceInput, #dsInput, #dsOutput, #req, #resp）
 * 2. 遍历 ServiceMapping.outputMapping
 * 3. 对每个 FieldBinding，使用 SpEL 解析表达式
 * 4. 构建符合 ApiService.contract.outputSchema 的 JsonNode
 *
 * @author renc
 */
@Service
public class OutputMappingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final EmbeddedContextExpressionParser expressionParser;

    public OutputMappingService() {
        this.expressionParser = new ExpressionContextParser();
    }

    /**
     * 执行输出映射
     *
     * @param serviceInput ApiService 的原始输入
     * @param dsInput Datasource 的输入（经过 input mapping 后）
     * @param dsOutput Datasource 的输出
     * @param req Connector 的请求对象（可选）
     * @param resp Connector 的响应对象（可选）
     * @param mapping ServiceMapping（包含 outputMapping 规则）
     * @return ApiService 的输出（符合 ServiceContract.outputSchema）
     */
    public JsonNode mapOutput(JsonNode serviceInput, JsonNode dsInput, JsonNode dsOutput,
                              Object req, Object resp, ServiceMapping mapping) {
        if (mapping == null || mapping.outputMapping().isEmpty()) {
            // 如果没有映射规则，直接返回 dsOutput
            return dsOutput != null ? dsOutput : OBJECT_MAPPER.createObjectNode();
        }

        // 构建输出对象
        ObjectNode serviceOutput = OBJECT_MAPPER.createObjectNode();

        // 遍历映射规则
        for (Map.Entry<String, FieldBinding> entry : mapping.outputMapping().entrySet()) {
            String targetField = entry.getKey();
            FieldBinding binding = entry.getValue();

            // 解析表达式
            Map<String, Object> rootMap = createRootMap(serviceInput, dsInput, dsOutput, req, resp);
            Object value = parseExpression(binding.expression(), rootMap);

            // 设置到输出对象
            setJsonNodeValue(serviceOutput, targetField, value);
        }

        return serviceOutput;
    }

    /**
     * 创建变量映射（作为 SpEL 解析的 root）
     */
    private Map<String, Object> createRootMap(JsonNode serviceInput, JsonNode dsInput,
                                               JsonNode dsOutput, Object req, Object resp) {
        Map<String, Object> rootMap = new java.util.HashMap<>();
        rootMap.put("serviceInput", serviceInput);
        rootMap.put("dsInput", dsInput);
        rootMap.put("dsOutput", dsOutput);
        rootMap.put("req", req);
        rootMap.put("resp", resp);
        return rootMap;
    }

    /**
     * 解析 SpEL 表达式
     */
    private Object parseExpression(String expression, Map<String, Object> rootMap) {
        try {
            return expressionParser.parseValue(expression, rootMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse expression: " + expression, e);
        }
    }

    /**
     * 设置 JsonNode 的值（支持嵌套路径，如 "user.name"）
     */
    private void setJsonNodeValue(ObjectNode node, String fieldPath, Object value) {
        if (fieldPath.contains(".")) {
            // 嵌套路径处理
            String[] parts = fieldPath.split("\\.", 2);
            String parent = parts[0];
            String child = parts[1];

            JsonNode parentNode = node.get(parent);
            if (parentNode == null || !parentNode.isObject()) {
                parentNode = OBJECT_MAPPER.createObjectNode();
                node.set(parent, parentNode);
            }

            setJsonNodeValue((ObjectNode) parentNode, child, value);
        } else {
            // 直接设置
            if (value instanceof JsonNode) {
                node.set(fieldPath, (JsonNode) value);
            } else if (value != null) {
                node.putPOJO(fieldPath, value);
            }
        }
    }
}
