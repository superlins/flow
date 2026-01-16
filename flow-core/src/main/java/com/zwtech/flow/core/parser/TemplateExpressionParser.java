package com.zwtech.flow.core.parser;

import com.zwtech.flow.core.DefaultVariableContext;
import com.zwtech.flow.core.VariableContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Template expression parser that supports both template syntax {{ }} and SpEL expressions
 *
 * @author renc
 */
public final class TemplateExpressionParser {

    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");

    public TemplateExpressionParser() {
    }

    /**
     * Parse a template string (contains {{ }} expressions)
     */
    public Object parseTemplate(String template, VariableContext context) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        if (!matcher.find()) {
            // No template expressions found, return as-is
            return template;
        }

        StringBuilder result = new StringBuilder();
        matcher.reset();
        while (matcher.find()) {
            String expression = matcher.group(1).trim();
            Object value = evaluateSpel(expression, context);
            matcher.appendReplacement(result, value != null ? String.valueOf(value) : "");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Parse an object which may contain template expressions or SpEL expressions
     */
    public Object parseObject(Object input, VariableContext context) {
        if (input == null) {
            return null;
        }

        if (input instanceof String) {
            String str = (String) input;
            Matcher matcher = TEMPLATE_PATTERN.matcher(str);
            if (matcher.find()) {
                return parseTemplate(str, context);
            }
            // Try direct SpEL evaluation
            try {
                return evaluateSpel(str, context);
            } catch (Exception e) {
                return str;
            }
        }

        if (input instanceof Map) {
            ObjectNode resultNode = OBJECT_MAPPER.createObjectNode();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) input;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = parseObject(entry.getValue(), context);
                if (value instanceof JsonNode) {
                    resultNode.set(entry.getKey(), (JsonNode) value);
                } else {
                    resultNode.putPOJO(entry.getKey(), value);
                }
            }
            return resultNode;
        }

        return input;
    }

    /**
     * Evaluate a SpEL expression in the given variable context
     */
    private Object evaluateSpel(String expression, VariableContext context) {
        try {
            Expression exp = SPEL_PARSER.parseExpression(expression);
            EvaluationContext evalContext = buildEvaluationContext(context);
            Object value = exp.getValue(evalContext);

            // Convert JsonNode to Java types for simpler handling
            if (value instanceof JsonNode) {
                JsonNode node = (JsonNode) value;
                if (node.isTextual()) {
                    return node.asText();
                } else if (node.isInt()) {
                    return node.asInt();
                } else if (node.isLong()) {
                    return node.asLong();
                } else if (node.isDouble()) {
                    return node.asDouble();
                } else if (node.isBoolean()) {
                    return node.asBoolean();
                } else if (node.isArray()) {
                    ArrayNode array = (ArrayNode) node;
                    Object[] arr = new Object[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        arr[i] = array.get(i);
                    }
                    return arr;
                } else if (node.isObject()) {
                    Map<String, Object> map = new HashMap<>();
                    node.fields().forEachRemaining(entry ->
                        map.put(entry.getKey(), entry.getValue())
                    );
                    return map;
                }
            }

            return value;
        } catch (SpelEvaluationException e) {
            // Return expression as-is if evaluation fails
            return expression;
        }
    }

    /**
     * Build Spring EvaluationContext from VariableContext
     */
    private EvaluationContext buildEvaluationContext(VariableContext context) {
        StandardEvaluationContext evalContext = new StandardEvaluationContext();

        // Define variables based on VariableContext implementation
        if (context instanceof DefaultVariableContext) {
            DefaultVariableContext dvc = (DefaultVariableContext) context;
            evalContext.setVariable("request", dvc.getRequest());
            evalContext.setVariable("response", dvc.getResponse());
        } else {
            // For other VariableContext implementations, add them with generic names
            evalContext.setVariable("context", context);
        }

        return evalContext;
    }
}
