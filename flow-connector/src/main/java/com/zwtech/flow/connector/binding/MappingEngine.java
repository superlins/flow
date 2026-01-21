package com.zwtech.flow.connector.binding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zwtech.flow.core.VariableContext;
import com.zwtech.flow.core.parser.TemplateExpressionParser;
import com.zwtech.flow.domain.shared.MappingSpec;
import com.zwtech.flow.domain.shared.mapping.ExpressionNode;
import com.zwtech.flow.domain.shared.mapping.MappingNode;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 统一映射引擎
 * <p>
 * 基于 {@link MappingSpec} 执行表达式解析和字段映射。
 * 整合了原有分散在 RequestBinder 和 ResponseConverter 中的映射处理逻辑。
 * <p>
 * 核心能力：
 * <ul>
 * <li>根据 MappingSpec 应用字段映射，生成目标 JsonNode</li>
 * <li>解析单个 SpEL 表达式</li>
 * <li>支持嵌套路径设置（如 "user.name"）</li>
 * <li>兼容原有字符串模板格式</li>
 * </ul>
 *
 * @author renc
 */
@Component
public class MappingEngine {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final TemplateExpressionParser templateParser;

    public MappingEngine() {
        this.templateParser = new TemplateExpressionParser();
    }

    /**
     * 应用映射规则，生成目标 JsonNode
     *
     * @param spec    映射规格
     * @param context 变量上下文（包含 request、response 等）
     * @return 映射结果
     */
    public JsonNode applyMappings(MappingSpec spec, VariableContext context) {
        if (spec == null || spec.isEmpty()) {
            return OBJECT_MAPPER.createObjectNode();
        }

        // 递归处理 MappingNode
        return applyMappingNode(spec.root(), context);
    }

    /**
     * 递归应用 MappingNode
     *
     * @param node    映射节点
     * @param context 变量上下文
     * @return JsonNode
     */
    private JsonNode applyMappingNode(MappingNode node, VariableContext context) {
        return switch (node) {
            case ExpressionNode expr -> {
                Object value = parseExpression(expr.expression(), context);
                yield OBJECT_MAPPER.valueToTree(value);
            }
            case com.zwtech.flow.domain.shared.mapping.ObjectNode obj -> {
                ObjectNode result = OBJECT_MAPPER.createObjectNode();
                obj.fields().forEach((fieldName, fieldNode) -> {
                    JsonNode fieldValue = applyMappingNode(fieldNode, context);
                    result.set(fieldName, fieldValue);
                });
                yield result;
            }
            case com.zwtech.flow.domain.shared.mapping.ArrayNode arr -> {
                ArrayNode result = OBJECT_MAPPER.createArrayNode();
                if (arr.isStatic()) {
                    // 静态数组：直接处理每个元素
                    for (MappingNode element : arr.elements()) {
                        result.add(applyMappingNode(element, context));
                    }
                } else if (arr.isDynamic()) {
                    // 动态数组：循环处理
                    Object loopSource = parseExpression(arr.loopExpression(), context);
                    if (loopSource instanceof Iterable<?> items) {
                        for (Object item : items) {
                            // 创建子上下文，将 item 添加为 "item" 变量
                            VariableContext itemContext = context.createChild();
                            itemContext.setVariable("item", item);
                            result.add(applyMappingNode(arr.itemTemplate(), itemContext));
                        }
                    }
                }
                yield result;
            }
        };
    }

    /**
     * 解析单个表达式
     *
     * @param expression SpEL 表达式或模板字符串
     * @param context    变量上下文
     * @return 解析结果
     */
    public Object parseExpression(String expression, VariableContext context) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        return templateParser.parseTemplate(expression, context);
    }

    /**
     * 解析模板字符串为 JsonNode
     * <p>
     * 兼容原有模板格式，支持整个 JSON 模板的解析。
     *
     * @param template JSON 模板字符串（可包含表达式）
     * @param context  变量上下文
     * @return 解析后的 JsonNode
     */
    public JsonNode parseTemplate(String template, VariableContext context) {
        if (template == null || template.isBlank()) {
            return OBJECT_MAPPER.createObjectNode();
        }

        Object result = templateParser.parseTemplate(template, context);
        if (result instanceof JsonNode jsonNode) {
            return jsonNode;
        }
        return OBJECT_MAPPER.valueToTree(result);
    }

    /**
     * 将 Map 形式的映射转换为 MappingSpec（扁平化版本）
     *
     * @param mappings 字段映射 Map
     * @return MappingSpec 对象
     */
    public MappingSpec toMappingSpec(Map<String, String> mappings) {
        return MappingSpec.ofFlat(mappings);
    }
}
