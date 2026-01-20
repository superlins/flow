package com.zwtech.flow.domain.shared;

import java.util.Map;
import java.util.Objects;

/**
 * 通用映射规格
 * <p>
 * 定义字段映射规则，支持 SpEL 表达式。
 * 用于替代原有的字符串模板（如 headersTemplate, bodyTemplate 等），
 * 提供更结构化和可扩展的映射能力。
 * <p>
 * 使用示例：
 * 
 * <pre>
 * MappingSpec headers = MappingSpec.of(Map.of(
 *         "Authorization", "#request.token",
 *         "Content-Type", "'application/json'"));
 * </pre>
 *
 * @author renc
 */
public record MappingSpec(
        Map<String, String> fieldMappings // fieldName -> SpEL expression
) implements ValueObject<MappingSpec> {

    /**
     * 规范化构造：确保 fieldMappings 为不可变 Map
     */
    public MappingSpec {
        fieldMappings = fieldMappings != null ? Map.copyOf(fieldMappings) : Map.of();
    }

    /**
     * 创建空映射
     */
    public static MappingSpec empty() {
        return new MappingSpec(Map.of());
    }

    /**
     * 从 Map 创建映射
     *
     * @param mappings 字段名 -> SpEL 表达式
     */
    public static MappingSpec of(Map<String, String> mappings) {
        return new MappingSpec(mappings);
    }

    /**
     * 判断是否为空映射
     */
    public boolean isEmpty() {
        return fieldMappings.isEmpty();
    }

    /**
     * 获取映射条目数
     */
    public int size() {
        return fieldMappings.size();
    }

    /**
     * 合并另一个 MappingSpec（后者覆盖前者）
     *
     * @param other 要合并的映射
     * @return 新的合并后的 MappingSpec
     */
    public MappingSpec merge(MappingSpec other) {
        if (other == null || other.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return other;
        }
        var merged = new java.util.HashMap<>(this.fieldMappings);
        merged.putAll(other.fieldMappings);
        return new MappingSpec(merged);
    }

    @Override
    public boolean sameValueAs(MappingSpec other) {
        return other != null && fieldMappings.equals(other.fieldMappings);
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
        return Objects.hash(fieldMappings);
    }
}
