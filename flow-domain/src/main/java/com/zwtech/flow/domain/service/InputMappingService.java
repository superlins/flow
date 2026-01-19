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
 * InputMappingService
 * 
 * 负责将 ApiService 的输入映射到 Datasource 的输入
 * 
 * 执行流程：
 * 1. 创建变量上下文（包含 #serviceInput, #env）
 * 2. 遍历 ServiceMapping.inputMapping
 * 3. 对每个 FieldBinding，使用 SpEL 解析表达式
 * 4. 构建符合 Datasource.contract.inputSchema 的 JsonNode
 *
 * @author renc
 */
@Service
public class InputMappingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final EmbeddedContextExpressionParser expressionParser;

    public InputMappingService() {
        this.expressionParser = new ExpressionContextParser();
    }

    /**
     * 执行输入映射
     * 
     * @param serviceInput ApiService 的输入（已通过 ServiceContract.inputSchema 校验）
     * @param mapping ServiceMapping（包含 inputMapping 规则）
     * @param env 环境变量（可选）
     * @return Datasource 的输入（符合 DatasourceContract.inputSchema）
     */
    public JsonNode mapInput(JsonNode serviceInput, ServiceMapping mapping, JsonNode env) {
        if (mapping == null || mapping.inputMapping().isEmpty()) {
            // 如果没有映射规则，直接返回 serviceInput
            return serviceInput;
        }

        // 创建变量上下文
        VariableContext variableContext = createVariableContext(serviceInput, env);

        // 构建输出对象
        ObjectNode dsInput = OBJECT_MAPPER.createObjectNode();

        // 遍历映射规则
        for (Map.Entry<String, FieldBinding> entry : mapping.inputMapping().entrySet()) {
            String targetField = entry.getKey();
            FieldBinding binding = entry.getValue();
            
            // 解析表达式
            Object value = parseExpression(binding.expression(), variableContext);
            
            // 设置到输出对象
            setJsonNodeValue(dsInput, targetField, value);
        }

        return dsInput;
    }

    /**
     * 创建变量上下文
     */
    private VariableContext createVariableContext(JsonNode serviceInput, JsonNode env) {
        DefaultVariableContext context = new DefaultVariableContext(serviceInput, null, null, null, null);
        if (env != null) {
            context.getEvaluationContext().setVariable("env", env);
        }
        return context;
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
