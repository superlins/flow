package com.zwtech.flow.domain.shared.mapping;

/**
 * 映射节点（支持递归嵌套结构）
 * <p>
 * 这是一个 sealed interface，用于表达可嵌套的映射结构。
 * 支持三种节点类型：
 * <ul>
 * <li>{@link ExpressionNode} - 表达式叶子节点（SpEL 表达式）</li>
 * <li>{@link ObjectNode} - 对象节点（Map 结构，支持嵌套）</li>
 * <li>{@link ArrayNode} - 数组节点（静态/动态数组）</li>
 * </ul>
 * <p>
 * <b>设计意图：</b>
 * <ul>
 * <li>支持任意层级的嵌套结构（类似 JSON Schema）</li>
 * <li>每个节点可以是表达式、对象或数组</li>
 * <li>提供类型安全的编译期检查（sealed interface）</li>
 * <li>领域模型只关注"结构定义"，不关心"执行逻辑"</li>
 * </ul>
 * <p>
 * <b>业务规则：</b>
 * <ul>
 * <li>MN-1: 节点深度不能超过限制（默认 10 层，防止过深嵌套）</li>
 * <li>MN-2: 表达式节点不能为空字符串</li>
 * <li>MN-3: 对象节点的字段名不能为空</li>
 * <li>MN-4: 数组节点必须是静态或动态之一（不能同时为空）</li>
 * </ul>
 *
 * @author renc
 */
@com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, property = "nodeType")
@com.fasterxml.jackson.annotation.JsonSubTypes({
        @com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = ExpressionNode.class, name = "EXPRESSION"),
        @com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = ObjectNode.class, name = "OBJECT"),
        @com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = ArrayNode.class, name = "ARRAY")
})
public sealed interface MappingNode permits ExpressionNode, ObjectNode, ArrayNode {

    /**
     * 获取节点类型
     *
     * @return 节点类型枚举
     */
    NodeType nodeType();

    /**
     * 计算当前节点的最大深度
     * <p>
     * 叶子节点深度为 1，对象/数组节点深度为 1 + max(子节点深度)
     *
     * @return 节点深度（从 1 开始）
     */
    int depth();

    /**
     * 验证节点是否满足业务规则
     * <p>
     * 在节点创建时自动调用，确保领域不变量
     *
     * @throws IllegalArgumentException 如果违反业务规则
     */
    default void validate() {
        // 默认实现：检查深度限制（MN-1）
        if (depth() > MAX_DEPTH) {
            throw new IllegalArgumentException(
                    "Mapping node depth exceeds maximum allowed depth: " + MAX_DEPTH
                            + ", actual: " + depth());
        }
    }

    /**
     * 最大嵌套深度限制（业务规则 MN-1）
     */
    int MAX_DEPTH = 10;

    /**
     * 节点类型枚举
     */
    enum NodeType {
        /**
         * 表达式节点（叶子节点）
         */
        EXPRESSION,

        /**
         * 对象节点（可嵌套）
         */
        OBJECT,

        /**
         * 数组节点（可包含静态元素或动态模板）
         */
        ARRAY
    }
}
