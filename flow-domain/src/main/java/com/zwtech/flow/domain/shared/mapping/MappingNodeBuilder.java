package com.zwtech.flow.domain.shared.mapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 映射节点构建器（领域服务）
 * <p>
 * 提供流畅的 API 来构建复杂的 MappingNode 树形结构。
 * <p>
 * <b>设计意图：</b>
 * <ul>
 * <li>简化嵌套映射的构建过程</li>
 * <li>提供类型安全的构建方式</li>
 * <li>支持链式调用</li>
 * <li>构建过程中进行基础验证</li>
 * </ul>
 * <p>
 * <b>使用示例：</b>
 * 
 * <pre>
 * // 构建嵌套对象
 * var mapping = MappingNodeBuilder.object()
 *         .field("userId", expr("{{ #dsInput.userId }}"))
 *         .field("user", object()
 *                 .field("name", expr("{{ #dsInput.userName }}"))
 *                 .field("age", expr("{{ #dsInput.age }}"))
 *                 .build())
 *         .build();
 * 
 * // 构建动态数组
 * var orders = MappingNodeBuilder.array()
 *         .dynamicItems(
 *                 object()
 *                         .field("orderId", expr("{{ item.id }}"))
 *                         .field("amount", expr("{{ item.price }}"))
 *                         .build(),
 *                 "#dsInput.orderList")
 *         .build();
 * </pre>
 *
 * @author renc
 */
public class MappingNodeBuilder {

    /**
     * 创建表达式节点
     *
     * @param expression SpEL 表达式
     * @return ExpressionNode
     */
    public static ExpressionNode expr(String expression) {
        return new ExpressionNode(expression);
    }

    /**
     * 创建对象节点构建器
     *
     * @return ObjectNodeBuilder
     */
    public static ObjectNodeBuilder object() {
        return new ObjectNodeBuilder();
    }

    /**
     * 创建数组节点构建器
     *
     * @return ArrayNodeBuilder
     */
    public static ArrayNodeBuilder array() {
        return new ArrayNodeBuilder();
    }

    /**
     * 对象节点构建器
     */
    public static class ObjectNodeBuilder {
        private final Map<String, MappingNode> fields = new LinkedHashMap<>();

        /**
         * 添加字段
         *
         * @param name 字段名
         * @param node 字段值节点
         * @return this
         */
        public ObjectNodeBuilder field(String name, MappingNode node) {
            fields.put(name, node);
            return this;
        }

        /**
         * 添加表达式字段（语法糖）
         *
         * @param name       字段名
         * @param expression SpEL 表达式
         * @return this
         */
        public ObjectNodeBuilder field(String name, String expression) {
            fields.put(name, new ExpressionNode(expression));
            return this;
        }

        /**
         * 添加嵌套对象字段（语法糖）
         *
         * @param name          字段名
         * @param objectBuilder 嵌套对象构建器
         * @return this
         */
        public ObjectNodeBuilder object(String name, ObjectNodeBuilder objectBuilder) {
            fields.put(name, objectBuilder.build());
            return this;
        }

        /**
         * 添加数组字段（语法糖）
         *
         * @param name         字段名
         * @param arrayBuilder 数组构建器
         * @return this
         */
        public ObjectNodeBuilder array(String name, ArrayNodeBuilder arrayBuilder) {
            fields.put(name, arrayBuilder.build());
            return this;
        }

        /**
         * 构建 ObjectNode
         *
         * @return ObjectNode
         */
        public ObjectNode build() {
            return new ObjectNode(fields);
        }
    }

    /**
     * 数组节点构建器
     */
    public static class ArrayNodeBuilder {
        private List<MappingNode> elements;
        private MappingNode itemTemplate;
        private String loopExpression;

        /**
         * 添加静态元素
         *
         * @param node 元素节点
         * @return this
         */
        public ArrayNodeBuilder element(MappingNode node) {
            if (elements == null) {
                elements = new ArrayList<>();
            }
            elements.add(node);
            return this;
        }

        /**
         * 添加静态表达式元素（语法糖）
         *
         * @param expression SpEL 表达式
         * @return this
         */
        public ArrayNodeBuilder element(String expression) {
            return element(new ExpressionNode(expression));
        }

        /**
         * 设置动态数组配置
         *
         * @param template       元素模板
         * @param loopExpression 循环表达式
         * @return this
         */
        public ArrayNodeBuilder dynamicItems(MappingNode template, String loopExpression) {
            this.itemTemplate = template;
            this.loopExpression = loopExpression;
            return this;
        }

        /**
         * 构建 ArrayNode
         *
         * @return ArrayNode
         */
        public ArrayNode build() {
            return new ArrayNode(elements, itemTemplate, loopExpression);
        }
    }

    // ========== 便捷工厂方法 ==========

    /**
     * 快速创建单个表达式字段的对象
     *
     * @param fieldName  字段名
     * @param expression 表达式
     * @return ObjectNode
     */
    public static ObjectNode singleFieldObject(String fieldName, String expression) {
        return object().field(fieldName, expression).build();
    }

    /**
     * 从扁平化 Map 创建对象节点
     *
     * @param flatMap 字段名 -> 表达式
     * @return ObjectNode
     */
    public static ObjectNode fromFlatMap(Map<String, String> flatMap) {
        ObjectNodeBuilder builder = object();
        flatMap.forEach(builder::field);
        return builder.build();
    }

    /**
     * 创建单个表达式元素的静态数组
     *
     * @param expressions 表达式列表
     * @return ArrayNode
     */
    public static ArrayNode staticArray(String... expressions) {
        ArrayNodeBuilder builder = array();
        for (String expr : expressions) {
            builder.element(expr);
        }
        return builder.build();
    }
}
