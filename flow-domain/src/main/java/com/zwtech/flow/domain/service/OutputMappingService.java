package com.zwtech.flow.domain.service;

import com.zwtech.flow.core.DefaultVariableContext;
import com.zwtech.flow.core.VariableContext;
import com.zwtech.flow.core.parser.spel.EmbeddedContextExpressionParser;
import com.zwtech.flow.core.parser.spel.ExpressionContextParser;
import com.zwtech.flow.domain.model.apiservice.FieldBinding;
import com.zwtech.flow.domain.model.apiservice.ServiceMapping;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

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

        // 创建变量上下文
        VariableContext variableContext = createVariableContext(serviceInput, dsInput, dsOutput, req, resp);

        // 构建输出对象
        ObjectNode serviceOutput = OBJECT_MAPPER.createObjectNode();

        // 遍历映射规则
        for (Map.Entry<String, FieldBinding> entry : mapping.outputMapping().entrySet()) {
            String targetField = entry.getKey();
            FieldBinding binding = entry.getValue();
            
            // 解析表达式
            Object value = parseExpression(binding.expression(), variableContext);
            
            // 设置到输出对象
            setJsonNodeValue(serviceOutput, targetField, value);
        }

        return serviceOutput;
    }

    /**
     * 创建变量上下文
     */
    private VariableContext createVariableContext(JsonNode serviceInput, JsonNode dsInput, 
                                                   JsonNode dsOutput, Object req, Object resp) {
        return new DefaultVariableContext(serviceInput, dsInput, dsOutput, req, resp);
    }

    /**
     * 解析 SpEL 表达式
     */
    private Object parseExpression(String expression, VariableContext variableContext) {
        if (variableContext instanceof DefaultVariableContext) {
            DefaultVariableContext defaultContext = (DefaultVariableContext) variableContext;
            return expressionParser.parseValue(expression, defaultContext.getEvaluationContext());
        }
        // 降级处理
        return expressionParser.parseValue(expression);
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
