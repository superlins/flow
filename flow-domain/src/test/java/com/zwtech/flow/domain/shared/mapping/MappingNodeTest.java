package com.zwtech.flow.domain.shared.mapping;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MappingNode 领域模型测试
 * <p>
 * 重点测试业务规则和领域不变量
 *
 * @author renc
 */
class MappingNodeTest {

        // ========== ExpressionNode Tests ==========

        @Test
        void testExpressionNode_basicCreation() {
                var node = new ExpressionNode("{{ #dsInput.userId }}");
                assertEquals("{{ #dsInput.userId }}", node.expression());
                assertEquals(MappingNode.NodeType.EXPRESSION, node.nodeType());
                assertEquals(1, node.depth());
        }

        @Test
        void testExpressionNode_rejectsNullOrEmpty() {
                // MN-2: 表达式不能为空
                assertThrows(IllegalArgumentException.class,
                                () -> new ExpressionNode(null));
                assertThrows(IllegalArgumentException.class,
                                () -> new ExpressionNode(""));
                assertThrows(IllegalArgumentException.class,
                                () -> new ExpressionNode("   "));
        }

        @Test
        void testExpressionNode_rejectsTooLong() {
                // MN-2.1: 表达式长度限制
                String tooLong = "x".repeat(ExpressionNode.MAX_EXPRESSION_LENGTH + 1);
                assertThrows(IllegalArgumentException.class,
                                () -> new ExpressionNode(tooLong));
        }

        @Test
        void testExpressionNode_templateDetection() {
                var templateExpr = new ExpressionNode("{{ #dsInput.userId }}");
                assertTrue(templateExpr.isTemplate());

                var constantExpr = new ExpressionNode("'FIXED_VALUE'");
                assertTrue(constantExpr.isConstant());
                assertFalse(constantExpr.isTemplate());
        }

        @Test
        void testExpressionNode_unwrap() {
                var node = new ExpressionNode("{{ #dsInput.userId }}");
                assertEquals("#dsInput.userId", node.unwrappedExpression());

                var plainNode = new ExpressionNode("#dsInput.userId");
                assertEquals("#dsInput.userId", plainNode.unwrappedExpression());
        }

        // ========== ObjectNode Tests ==========

        @Test
        void testObjectNode_basicCreation() {
                var node = new ObjectNode(Map.of(
                                "userId", new ExpressionNode("{{ #dsInput.userId }}"),
                                "userName", new ExpressionNode("{{ #dsInput.userName }}")));

                assertEquals(MappingNode.NodeType.OBJECT, node.nodeType());
                assertEquals(2, node.size());
                assertEquals(2, node.depth()); // 1 (Object) + 1 (Expression) = 2
        }

        @Test
        void testObjectNode_nestedStructure() {
                // 嵌套 3 层
                var profileNode = new ObjectNode(Map.of(
                                "age", new ExpressionNode("{{ #dsInput.age }}"),
                                "email", new ExpressionNode("{{ #dsInput.email }}")));

                var userNode = new ObjectNode(Map.of(
                                "id", new ExpressionNode("{{ #dsInput.userId }}"),
                                "profile", profileNode));

                assertEquals(3, userNode.depth()); // 1(userNode) + max(1(id), 2(profile)) = 3

                assertTrue(userNode.hasField("id"));
                assertTrue(userNode.hasField("profile"));
                assertFalse(userNode.hasField("nonExistent"));
        }

        @Test
        void testObjectNode_rejectsNullFieldValue() {
                // MN-3.2: 字段值不能为 null
                // 使用 HashMap 允许 null 值，以验证 ObjectNode 的检查逻辑
                Map<String, MappingNode> fields = new java.util.HashMap<>();
                fields.put("userId", null);
                assertThrows(IllegalArgumentException.class,
                                () -> new ObjectNode(fields));
        }

        @Test
        void testObjectNode_rejectsEmptyFieldName() {
                // MN-3: 字段名不能为空
                assertThrows(IllegalArgumentException.class,
                                () -> new ObjectNode(Map.of("", new ExpressionNode("value"))));
        }

        @Test
        void testObjectNode_rejectsTooManyFields() {
                // MN-3.3: 字段数量限制
                var fields = new java.util.HashMap<String, MappingNode>();
                for (int i = 0; i <= ObjectNode.MAX_FIELDS_COUNT; i++) {
                        fields.put("field" + i, new ExpressionNode("value" + i));
                }
                assertThrows(IllegalArgumentException.class,
                                () -> new ObjectNode(fields));
        }

        @Test
        void testObjectNode_empty() {
                var empty = ObjectNode.empty();
                assertTrue(empty.isEmpty());
                assertEquals(0, empty.size());
                assertEquals(1, empty.depth());
        }

        // ========== ArrayNode Tests ==========

        @Test
        void testArrayNode_staticCreation() {
                var node = ArrayNode.ofStatic(
                                new ExpressionNode("'VIP'"),
                                new ExpressionNode("'PREMIUM'"),
                                new ExpressionNode("{{ #dsInput.level }}"));

                assertEquals(MappingNode.NodeType.ARRAY, node.nodeType());
                assertTrue(node.isStatic());
                assertFalse(node.isDynamic());
                assertEquals(3, node.size());
                assertEquals(2, node.depth()); // 1 (Array) + 1 (Expression) = 2
        }

        @Test
        void testArrayNode_dynamicCreation() {
                var itemTemplate = new ObjectNode(Map.of(
                                "orderId", new ExpressionNode("{{ item.id }}"),
                                "amount", new ExpressionNode("{{ item.price }}")));

                var node = ArrayNode.ofDynamic(itemTemplate, "#dsInput.orderList");

                assertTrue(node.isDynamic());
                assertFalse(node.isStatic());
                assertEquals("#dsInput.orderList", node.loopExpression());
                assertEquals(0, node.size()); // 动态数组 size 为 0
        }

        @Test
        void testArrayNode_rejectsInvalidState() {
                // MN-4: 必须是静态或动态之一（不能同时为空）
                assertThrows(IllegalArgumentException.class,
                                () -> new ArrayNode(null, null, null));

                // 不能同时设置静态和动态
                assertThrows(IllegalArgumentException.class,
                                () -> new ArrayNode(
                                                List.of(new ExpressionNode("value")),
                                                new ExpressionNode("template"),
                                                "#dsInput.list"));
        }

        @Test
        void testArrayNode_rejectsNullElement() {
                // MN-4.1: 静态数组元素不能为 null
                // 使用 ArrayList 允许 null 元素
                List<MappingNode> elements = new java.util.ArrayList<>();
                elements.add(null);
                assertThrows(IllegalArgumentException.class,
                                () -> new ArrayNode(elements, null, null));
        }

        @Test
        void testArrayNode_rejectsTooManyElements() {
                // MN-4.2: 静态数组元素数量限制
                var elements = new java.util.ArrayList<MappingNode>();
                for (int i = 0; i <= ArrayNode.MAX_STATIC_ELEMENTS; i++) {
                        elements.add(new ExpressionNode("value" + i));
                }
                assertThrows(IllegalArgumentException.class,
                                () -> ArrayNode.ofStatic(elements));
        }

        @Test
        void testArrayNode_dynamicRequiresBothFields() {
                // MN-4.3: 动态数组需要 template 和 loopExpression
                assertThrows(IllegalArgumentException.class,
                                () -> ArrayNode.ofDynamic(new ExpressionNode("template"), null));

                assertThrows(IllegalArgumentException.class,
                                () -> ArrayNode.ofDynamic(null, "#dsInput.list"));
        }

        // ========== 深度限制测试 ==========

        @Test
        void testMappingNode_depthLimit() {
                // MN-1: 深度限制
                // 构建一个边界内的深度结构 (depth = MAX_DEPTH)
                MappingNode node = new ExpressionNode("value"); // depth 1
                // 循环 MAX_DEPTH - 1 次，最终 depth = 1 + (MAX_DEPTH - 1) = MAX_DEPTH
                for (int i = 0; i < MappingNode.MAX_DEPTH - 1; i++) {
                        node = new ObjectNode(Map.of("nested", node));
                }

                // 验证当前深度是允许的
                assertEquals(MappingNode.MAX_DEPTH, node.depth());

                // 再加一层应该抛出异常
                final MappingNode deepNode = node;
                assertThrows(IllegalArgumentException.class,
                                () -> new ObjectNode(Map.of("tooDeep", deepNode)));
        }

        // ========== ValueObject 相等性测试 ==========

        @Test
        void testExpressionNode_equality() {
                var expr1 = new ExpressionNode("{{ #dsInput.userId }}");
                var expr2 = new ExpressionNode("{{ #dsInput.userId }}");
                var expr3 = new ExpressionNode("{{ #dsInput.userName }}");

                assertTrue(expr1.sameValueAs(expr2));
                assertFalse(expr1.sameValueAs(expr3));
                assertEquals(expr1, expr2);
                assertNotEquals(expr1, expr3);
        }

        @Test
        void testObjectNode_equality() {
                var obj1 = new ObjectNode(Map.of(
                                "id", new ExpressionNode("{{ #dsInput.userId }}")));
                var obj2 = new ObjectNode(Map.of(
                                "id", new ExpressionNode("{{ #dsInput.userId }}")));
                var obj3 = new ObjectNode(Map.of(
                                "id", new ExpressionNode("{{ #dsInput.userName }}")));

                assertTrue(obj1.sameValueAs(obj2));
                assertFalse(obj1.sameValueAs(obj3));
        }

        @Test
        void testArrayNode_equality() {
                var arr1 = ArrayNode.ofStatic(
                                new ExpressionNode("value1"),
                                new ExpressionNode("value2"));
                var arr2 = ArrayNode.ofStatic(
                                new ExpressionNode("value1"),
                                new ExpressionNode("value2"));
                var arr3 = ArrayNode.ofStatic(
                                new ExpressionNode("value3"));

                assertTrue(arr1.sameValueAs(arr2));
                assertFalse(arr1.sameValueAs(arr3));
        }
}
