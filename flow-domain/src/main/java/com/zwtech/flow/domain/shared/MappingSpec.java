package com.zwtech.flow.domain.shared;

import com.zwtech.flow.domain.shared.mapping.ArrayNode;
import com.zwtech.flow.domain.shared.mapping.ExpressionNode;
import com.zwtech.flow.domain.shared.mapping.MappingNode;
import com.zwtech.flow.domain.shared.mapping.ObjectNode;

import java.util.Map;
import java.util.Objects;

/**
 * 通用映射规格（支持嵌套结构）
 * <p>
 * 定义字段映射规则，支持 SpEL 表达式和任意层级的嵌套结构。
 * 使用 {@link MappingNode} 递归树形结构替代原有的扁平化 Map。
 * <p>
 * <b>设计意图：</b>
 * <ul>
 * <li>支持复杂嵌套的 JSON 结构（对象、数组、多层级）</li>
 * <li>提供类型安全的结构化映射能力</li>
 * <li>领域模型只关注"结构定义"，不关心"执行逻辑"</li>
 * <li>向后兼容：支持从扁平化 Map 创建</li>
 * </ul>
 * <p>
 * <b>使用示例：</b>
 * 
 * <pre>
 * // 扁平化映射（向后兼容）
 * MappingSpec headers = MappingSpec.ofFlat(Map.of(
 *         "Authorization", "{{ #request.token }}",
 *         "Content-Type", "'application/json'"));
 * 
 * // 嵌套对象映射
 * MappingSpec body = MappingSpec.ofObject(Map.of(
 *         "user", new ObjectNode(Map.of(
 *                 "id", new ExpressionNode("{{ #dsInput.userId }}"),
 *                 "profile", new ObjectNode(Map.of(
 *                         "name", new ExpressionNode("{{ #dsInput.userName }}")))))));
 * </pre>
 *
 * @param root 映射树的根节点
 * @author renc
 */
public record MappingSpec(MappingNode root) implements ValueObject<MappingSpec> {

    /**
     * 规范化构造：确保 root 不为 null
     */
    public MappingSpec {
        root = root != null ? root : ObjectNode.empty();
    }

    /**
     * 创建空映射（空对象节点）
     *
     * @return 空的 MappingSpec
     */
    public static MappingSpec empty() {
        return new MappingSpec(ObjectNode.empty());
    }

    /**
     * 从单个表达式创建映射
     *
     * @param expression SpEL 表达式
     * @return MappingSpec
     */
    public static MappingSpec ofExpression(String expression) {
        return new MappingSpec(new ExpressionNode(expression));
    }

    /**
     * 从对象字段创建映射
     *
     * @param fields 字段映射（字段名 -> MappingNode）
     * @return MappingSpec
     */
    public static MappingSpec ofObject(Map<String, MappingNode> fields) {
        return new MappingSpec(new ObjectNode(fields));
    }

    /**
     * 从扁平化 Map 创建映射（向后兼容）
     * <p>
     * 将 Map&lt;String, String&gt; 转换为 ObjectNode，每个值都是 ExpressionNode
     *
     * @param flatMappings 字段名 -> SpEL 表达式
     * @return MappingSpec
     */
    public static MappingSpec ofFlat(Map<String, String> flatMappings) {
        if (flatMappings == null || flatMappings.isEmpty()) {
            return empty();
        }
        Map<String, MappingNode> fields = flatMappings.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new ExpressionNode(e.getValue())));
        return new MappingSpec(new ObjectNode(fields));
    }

    /**
     * 从数组创建映射
     *
     * @param arrayNode 数组节点
     * @return MappingSpec
     */
    public static MappingSpec ofArray(ArrayNode arrayNode) {
        return new MappingSpec(arrayNode);
    }

    /**
     * 判断是否为空映射
     *
     * @return true 如果是空对象节点
     */
    public boolean isEmpty() {
        return root instanceof ObjectNode obj && obj.isEmpty();
    }

    /**
     * 获取映射深度
     *
     * @return 映射树的最大深度
     */
    public int depth() {
        return root.depth();
    }

    /**
     * 判断根节点是否为对象节点
     *
     * @return true 如果是对象节点
     */
    public boolean isObject() {
        return root instanceof ObjectNode;
    }

    /**
     * 判断根节点是否为数组节点
     *
     * @return true 如果是数组节点
     */
    public boolean isArray() {
        return root instanceof ArrayNode;
    }

    /**
     * 判断根节点是否为表达式节点
     *
     * @return true 如果是表达式节点
     */
    public boolean isExpression() {
        return root instanceof ExpressionNode;
    }

    /**
     * 获取根节点作为对象节点
     *
     * @return ObjectNode，如果不是对象节点则抛出异常
     * @throws IllegalStateException 如果根节点不是对象节点
     */
    public ObjectNode asObject() {
        if (root instanceof ObjectNode obj) {
            return obj;
        }
        throw new IllegalStateException("Root node is not an ObjectNode");
    }

    /**
     * 获取根节点作为数组节点
     *
     * @return ArrayNode，如果不是数组节点则抛出异常
     * @throws IllegalStateException 如果根节点不是数组节点
     */
    public ArrayNode asArray() {
        if (root instanceof ArrayNode arr) {
            return arr;
        }
        throw new IllegalStateException("Root node is not an ArrayNode");
    }

    /**
     * 获取根节点作为表达式节点
     *
     * @return ExpressionNode，如果不是表达式节点则抛出异常
     * @throws IllegalStateException 如果根节点不是表达式节点
     */
    public ExpressionNode asExpression() {
        if (root instanceof ExpressionNode expr) {
            return expr;
        }
        throw new IllegalStateException("Root node is not an ExpressionNode");
    }

    @Override
    public boolean sameValueAs(MappingSpec other) {
        return other != null && this.root.equals(other.root);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MappingSpec that = (MappingSpec) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
