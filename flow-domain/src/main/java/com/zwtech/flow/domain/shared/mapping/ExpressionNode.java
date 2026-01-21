package com.zwtech.flow.domain.shared.mapping;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

/**
 * 表达式节点（叶子节点）
 * <p>
 * 表示一个 SpEL 表达式字符串，是映射树的叶子节点。
 * <p>
 * <b>业务规则：</b>
 * <ul>
 * <li>MN-2: 表达式不能为 null 或空字符串</li>
 * <li>MN-2.1: 表达式长度不能超过 2048 字符</li>
 * </ul>
 * <p>
 * <b>使用示例：</b>
 * 
 * <pre>
 * // 引用变量
 * new ExpressionNode("{{ #dsInput.userId }}")
 * 
 * // 常量
 * new ExpressionNode("'FIXED_VALUE'")
 * 
 * // 表达式计算
 * new ExpressionNode("{{ #dsInput.price * 0.9 }}")
 * </pre>
 *
 * @param expression SpEL 表达式字符串（可包含 {{ }} 包装或直接表达式）
 * @author renc
 */
public record ExpressionNode(String expression) implements MappingNode, ValueObject<ExpressionNode> {

    /**
     * 表达式最大长度限制（业务规则 MN-2.1）
     */
    public static final int MAX_EXPRESSION_LENGTH = 2048;

    /**
     * 规范化构造：验证业务规则
     */
    public ExpressionNode(String expression) {
        Assert.hasText(expression, "Expression must not be empty (MN-2)");
        Assert.isTrue(expression.length() <= MAX_EXPRESSION_LENGTH,
                "Expression length exceeds maximum allowed: " + MAX_EXPRESSION_LENGTH
                        + ", actual: " + expression.length() + " (MN-2.1)");

        this.expression = expression;
        validate();
    }

    @Override
    public NodeType nodeType() {
        return NodeType.EXPRESSION;
    }

    @Override
    public int depth() {
        // 叶子节点深度为 1
        return 1;
    }

    /**
     * 判断是否为模板表达式（包含 {{ }} 包装）
     *
     * @return true 如果是模板表达式
     */
    public boolean isTemplate() {
        return expression.contains("{{") && expression.contains("}}");
    }

    /**
     * 判断是否为常量表达式（单引号包裹）
     *
     * @return true 如果是常量
     */
    public boolean isConstant() {
        return expression.trim().startsWith("'") && expression.trim().endsWith("'");
    }

    /**
     * 获取原始表达式（去除模板包装）
     * <p>
     * 如果表达式是 "{{ expr }}"，返回 "expr"；
     * 否则返回原始表达式
     *
     * @return 原始表达式字符串
     */
    public String unwrappedExpression() {
        String trimmed = expression.trim();
        if (trimmed.startsWith("{{") && trimmed.endsWith("}}")) {
            return trimmed.substring(2, trimmed.length() - 2).trim();
        }
        return expression;
    }

    @Override
    public boolean sameValueAs(ExpressionNode other) {
        return other != null && this.expression.equals(other.expression);
    }
}
