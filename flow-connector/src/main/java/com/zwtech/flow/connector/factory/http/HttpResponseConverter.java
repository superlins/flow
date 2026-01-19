package com.zwtech.flow.connector.factory.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zwtech.flow.connector.binding.ResponseConverter;
import com.zwtech.flow.connector.specs.DatasourceSpecs;
import com.zwtech.flow.connector.specs.HttpDatasourceSpecs;
import com.zwtech.flow.core.VariableContext;
import com.zwtech.flow.core.parser.spel.ExpressionContextParser;
import com.zwtech.flow.domain.model.apidatasource.operation.HttpDatasourceOperation;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * HTTP 响应转换器
 * <p>
 * 将 HttpResponseSpec 转换为 JsonNode，支持字段映射和 SpEL 表达式解析。
 * <p>
 * VariableContext 作为数据容器，所有变量提取都通过 SpEL 表达式引擎完成。
 *
 * @author renc
 */
public final class HttpResponseConverter
        implements ResponseConverter<HttpResponseSpec, HttpDatasourceSpecs> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ExpressionContextParser SPEL_PARSER = new ExpressionContextParser();

    public HttpResponseConverter() {
    }

    @Override
    public JsonNode convert(HttpResponseSpec response) {
        return OBJECT_MAPPER.valueToTree(response.getBody());
    }

    public JsonNode project(HttpResponseSpec response, HttpDatasourceOperation operation, VariableContext context) {
        ObjectNode outputNode = OBJECT_MAPPER.createObjectNode();
        String responseBodyTemplate = operation.responseBodyTemplate();

        if (responseBodyTemplate != null && !responseBodyTemplate.isEmpty()) {
            // 解析 JSON 模板字符串，提取字段映射
            try {
                JsonNode templateNode = OBJECT_MAPPER.readTree(responseBodyTemplate);
                if (templateNode.isObject()) {
                    templateNode.fields().forEachRemaining(entry -> {
                        String outputKey = entry.getKey();
                        String expression = entry.getValue().asText();

                        if (expression != null && !expression.isEmpty()) {
                            Object value = evaluateSpel(expression, context);
                            if (value instanceof JsonNode) {
                                outputNode.set(outputKey, (JsonNode) value);
                            } else if (value != null) {
                                outputNode.putPOJO(outputKey, value);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse responseBodyTemplate: " + responseBodyTemplate, e);
            }
        } else {
            // 如果没有映射配置，直接使用响应体
            JsonNode bodyNode = response.getBody();
            if (bodyNode != null) {
                if (bodyNode.isObject()) {
                    outputNode.setAll((ObjectNode) bodyNode);
                } else {
                    outputNode.setAll((ObjectNode) OBJECT_MAPPER.createObjectNode().set("data", bodyNode));
                }
            }
        }

        return outputNode;
    }

    /**
     * 兼容旧版本的 project 方法签名
     */
    @Override
    public JsonNode project(HttpResponseSpec response, DatasourceSpecs specs, VariableContext context) {
        var httpSpecs = (HttpDatasourceSpecs) specs;
        return project(response, httpSpecs.getOperation(), context);
    }

    @Override
    public JsonNode toVariableFormat(HttpResponseSpec response) {
        ObjectNode responseNode = OBJECT_MAPPER.createObjectNode();

        if (response.getStatusCode() != null) {
            responseNode.put("status", response.getStatusCode().value());
        }

        if (response.getBody() != null) {
            responseNode.set("body", response.getBody());
        }

        if (response.getHeaders() != null) {
            ObjectNode headersNode = OBJECT_MAPPER.createObjectNode();
            response.getHeaders().forEach((name, values) -> {
                if (values.size() == 1) {
                    headersNode.put(name, values.get(0));
                } else {
                    ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
                    values.forEach(arrayNode::add);
                    headersNode.set(name, arrayNode);
                }
            });
            responseNode.set("headers", headersNode);
        }

        return responseNode;
    }

    /**
     * 使用 SpEL 解析器评估表达式
     */
    private Object evaluateSpel(String expression, VariableContext context) {
        EvaluationContext evaluationContext = buildEvaluationContext(context);
        return SPEL_PARSER.parseValue(expression, evaluationContext);
    }

    /**
     * 将 VariableContext 转换为 Spring SpEL 的 EvaluationContext
     */
    private EvaluationContext buildEvaluationContext(VariableContext context) {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

        // 注册根对象（可以直接使用 root#field 访问）
        evaluationContext.setRootObject(context);

        // 注册常用变量
        context.getRequest().ifPresent(request -> evaluationContext.setVariable("request", request));
        context.getResponse().ifPresent(response -> evaluationContext.setVariable("response", response));

        // 注册所有自定义变量
        context.getVariables().forEach(evaluationContext::setVariable);

        return evaluationContext;
    }
}
