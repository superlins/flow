package com.zwtech.flow.domain.shared.mapping;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 数组节点
 * <p>
 * 支持两种模式：
 * <ul>
 * <li><b>静态数组</b>：直接指定每个元素的 MappingNode（elements 非空）</li>
 * <li><b>动态数组</b>：通过循环表达式和元素模板生成（itemTemplate + loopExpression 非空）</li>
 * </ul>
 * <p>
 * <b>业务规则：</b>
 * <ul>
 * <li>MN-4: 必须是静态或动态之一（不能同时为空，也不能同时设置）</li>
 * <li>MN-4.1: 静态数组元素不能为 null</li>
 * <li>MN-4.2: 静态数组元素数量不能超过 100 个</li>
 * <li>MN-4.3: 动态数组的 itemTemplate 和 loopExpression 必须同时存在</li>
 * <li>MN-4.4: loopExpression 不能为空字符串</li>
 * </ul>
 * <p>
 * <b>静态数组示例：</b>
 * 
 * <pre>
 * // 固定的标签数组
 * var tagsNode = ArrayNode.ofStatic(List.of(
 *         new ExpressionNode("'VIP'"),
 *         new ExpressionNode("'PREMIUM'"),
 *         new ExpressionNode("{{ #dsInput.userLevel }}")));
 * </pre>
 * <p>
 * <b>动态数组示例：</b>
 * 
 * <pre>
 * // 循环生成订单列表
 * var ordersNode = ArrayNode.ofDynamic(
 *         new ObjectNode(Map.of(
 *                 "orderId", new ExpressionNode("{{ item.id }}"),
 *                 "amount", new ExpressionNode("{{ item.price }}"))),
 *         "#dsInput.orderList" // 循环源
 * );
 * </pre>
 *
 * @param elements       静态元素列表（可选）
 * @param itemTemplate   动态元素模板（可选）
 * @param loopExpression 循环表达式（可选）
 * @author renc
 */
public record ArrayNode(
        List<MappingNode> elements,
        MappingNode itemTemplate,
        String loopExpression) implements MappingNode, ValueObject<ArrayNode> {

    /**
     * 静态数组最大元素数量限制（业务规则 MN-4.2）
     */
    public static final int MAX_STATIC_ELEMENTS = 100;

    /**
     * 规范化构造：验证业务规则并确保不可变性
     */
    public ArrayNode(
            List<MappingNode> elements,
            MappingNode itemTemplate,
            String loopExpression) {
        boolean hasStaticElements = elements != null && !elements.isEmpty();
        boolean hasDynamicTemplate = itemTemplate != null && loopExpression != null;

        // MN-4: 必须是静态或动态之一（不能同时为空，也不能同时设置）
        Assert.isTrue(hasStaticElements ^ hasDynamicTemplate,
                "ArrayNode must be either static (with elements) or dynamic (with itemTemplate + loopExpression), "
                        + "but not both or neither (MN-4)");

        if (hasStaticElements) {
            // MN-4.1: 元素不能为 null (在 List.copyOf 之前检查)
            for (int i = 0; i < elements.size(); i++) {
                Assert.notNull(elements.get(i),
                        "Static array element at index " + i + " must not be null (MN-4.1)");
            }

            // 静态数组验证
            this.elements = List.copyOf(elements);
            this.itemTemplate = null;
            this.loopExpression = null;

            // MN-4.2: 元素数量限制
            Assert.isTrue(this.elements.size() <= MAX_STATIC_ELEMENTS,
                    "Static array elements count exceeds maximum allowed: " + MAX_STATIC_ELEMENTS
                            + ", actual: " + this.elements.size() + " (MN-4.2)");
        } else {
            this.elements = null;
            this.itemTemplate = itemTemplate;
            this.loopExpression = loopExpression;
        }

        if (hasDynamicTemplate) {
            // 动态数组验证
            // MN-4.3: itemTemplate 和 loopExpression 必须同时存在
            Assert.notNull(this.itemTemplate, "Dynamic array itemTemplate must not be null (MN-4.3)");
            Assert.hasText(this.loopExpression, "Dynamic array loopExpression must not be empty (MN-4.4)");
        }

        validate();
    }

    @Override
    public NodeType nodeType() {
        return NodeType.ARRAY;
    }

    @Override
    public int depth() {
        if (isStatic()) {
            // 静态数组深度 = 1 + max(元素深度)
            return 1 + elements.stream()
                    .mapToInt(MappingNode::depth)
                    .max()
                    .orElse(0);
        } else {
            // 动态数组深度 = 1 + 模板深度
            return 1 + itemTemplate.depth();
        }
    }

    /**
     * 判断是否为静态数组
     *
     * @return true 如果是静态数组
     */
    public boolean isStatic() {
        return elements != null && !elements.isEmpty();
    }

    /**
     * 判断是否为动态数组
     *
     * @return true 如果是动态数组
     */
    public boolean isDynamic() {
        return itemTemplate != null && loopExpression != null;
    }

    /**
     * 获取静态数组元素数量
     *
     * @return 元素数量，如果是动态数组则返回 0
     */
    public int size() {
        return isStatic() ? elements.size() : 0;
    }

    /**
     * 创建静态数组节点
     *
     * @param elements 静态元素列表
     * @return ArrayNode
     */
    public static ArrayNode ofStatic(List<MappingNode> elements) {
        return new ArrayNode(elements, null, null);
    }

    /**
     * 创建静态数组节点（可变参数）
     *
     * @param elements 静态元素
     * @return ArrayNode
     */
    public static ArrayNode ofStatic(MappingNode... elements) {
        return new ArrayNode(List.of(elements), null, null);
    }

    /**
     * 创建动态数组节点
     *
     * @param itemTemplate   元素模板
     * @param loopExpression 循环表达式
     * @return ArrayNode
     */
    public static ArrayNode ofDynamic(MappingNode itemTemplate, String loopExpression) {
        return new ArrayNode(null, itemTemplate, loopExpression);
    }

    @Override
    public boolean sameValueAs(ArrayNode other) {
        if (other == null) {
            return false;
        }
        if (this.isStatic() && other.isStatic()) {
            return this.elements.equals(other.elements);
        }
        if (this.isDynamic() && other.isDynamic()) {
            return this.itemTemplate.equals(other.itemTemplate)
                    && this.loopExpression.equals(other.loopExpression);
        }
        return false;
    }
}
