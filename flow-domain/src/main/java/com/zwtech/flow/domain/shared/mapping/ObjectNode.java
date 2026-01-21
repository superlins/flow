package com.zwtech.flow.domain.shared.mapping;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * 对象节点（可嵌套）
 * <p>
 * 表示一个键值对结构（Map），每个值可以是任意类型的 {@link MappingNode}。
 * 支持任意层级的嵌套。
 * <p>
 * <b>业务规则：</b>
 * <ul>
 * <li>MN-3: 字段名不能为 null 或空字符串</li>
 * <li>MN-3.1: 字段名长度不能超过 128 字符</li>
 * <li>MN-3.2: 字段值不能为 null</li>
 * <li>MN-3.3: 对象字段数量不能超过 100 个</li>
 * </ul>
 * <p>
 * <b>使用示例：</b>
 * 
 * <pre>
 * var userNode = new ObjectNode(Map.of(
 *         "id", new ExpressionNode("{{ #dsInput.userId }}"),
 *         "name", new ExpressionNode("{{ #dsInput.userName }}"),
 *         "profile", new ObjectNode(Map.of(
 *                 "age", new ExpressionNode("{{ #dsInput.age }}"),
 *                 "email", new ExpressionNode("{{ #dsInput.email }}")))));
 * </pre>
 *
 * @param fields 字段映射（字段名 -> MappingNode）
 * @author renc
 */
public record ObjectNode(Map<String, MappingNode> fields) implements MappingNode, ValueObject<ObjectNode> {

    /**
     * 字段名最大长度限制（业务规则 MN-3.1）
     */
    public static final int MAX_FIELD_NAME_LENGTH = 128;

    /**
     * 对象最大字段数量限制（业务规则 MN-3.3）
     */
    public static final int MAX_FIELDS_COUNT = 100;

    /**
     * 规范化构造：验证业务规则并确保不可变性
     */
    public ObjectNode(Map<String, MappingNode> fields) {
        if (fields != null) {
            // 在 defensive copy 之前检查 null 值，避免 NPE (MN-3.2)
            for (Map.Entry<String, MappingNode> entry : fields.entrySet()) {
                Assert.notNull(entry.getValue(),
                        "Field value must not be null for field: " + entry.getKey() + " (MN-3.2)");
            }
            this.fields = Map.copyOf(fields);
        } else {
            this.fields = Map.of();
        }

        // 验证字段数量（MN-3.3）
        Assert.isTrue(this.fields.size() <= MAX_FIELDS_COUNT,
                "Object fields count exceeds maximum allowed: " + MAX_FIELDS_COUNT
                        + ", actual: " + this.fields.size() + " (MN-3.3)");

        // 验证每个字段（MN-3, MN-3.1, MN-3.2）
        for (Map.Entry<String, MappingNode> entry : this.fields.entrySet()) {
            String fieldName = entry.getKey();
            MappingNode fieldValue = entry.getValue();

            Assert.hasText(fieldName, "Field name must not be empty (MN-3)");
            Assert.isTrue(fieldName.length() <= MAX_FIELD_NAME_LENGTH,
                    "Field name length exceeds maximum allowed: " + MAX_FIELD_NAME_LENGTH
                            + ", actual: " + fieldName.length() + " (MN-3.1)");
            Assert.notNull(fieldValue, "Field value must not be null for field: " + fieldName + " (MN-3.2)");
        }

        validate();
    }

    @Override
    public NodeType nodeType() {
        return NodeType.OBJECT;
    }

    @Override
    public int depth() {
        if (fields.isEmpty()) {
            return 1;
        }
        // 对象节点深度 = 1 + max(子节点深度)
        return 1 + fields.values().stream()
                .mapToInt(MappingNode::depth)
                .max()
                .orElse(0);
    }

    /**
     * 判断是否为空对象
     *
     * @return true 如果没有字段
     */
    public boolean isEmpty() {
        return fields.isEmpty();
    }

    /**
     * 获取字段数量
     *
     * @return 字段数量
     */
    public int size() {
        return fields.size();
    }

    /**
     * 判断是否包含指定字段
     *
     * @param fieldName 字段名
     * @return true 如果包含该字段
     */
    public boolean hasField(String fieldName) {
        return fields.containsKey(fieldName);
    }

    /**
     * 获取指定字段的节点
     *
     * @param fieldName 字段名
     * @return 字段节点，如果不存在则返回 null
     */
    public MappingNode getField(String fieldName) {
        return fields.get(fieldName);
    }

    /**
     * 创建空对象节点
     *
     * @return 空的 ObjectNode
     */
    public static ObjectNode empty() {
        return new ObjectNode(Map.of());
    }

    /**
     * 创建单字段对象节点
     *
     * @param fieldName  字段名
     * @param fieldValue 字段值
     * @return ObjectNode
     */
    public static ObjectNode of(String fieldName, MappingNode fieldValue) {
        return new ObjectNode(Map.of(fieldName, fieldValue));
    }

    /**
     * 创建对象节点（工厂方法）
     *
     * @param fields 字段映射
     * @return ObjectNode
     */
    public static ObjectNode of(Map<String, MappingNode> fields) {
        return new ObjectNode(fields);
    }

    @Override
    public boolean sameValueAs(ObjectNode other) {
        return other != null && this.fields.equals(other.fields);
    }
}
