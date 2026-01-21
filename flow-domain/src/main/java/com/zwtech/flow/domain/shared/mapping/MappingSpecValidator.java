package com.zwtech.flow.domain.shared.mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * 映射规格验证器（领域服务）
 * <p>
 * 提供 MappingNode 的业务规则验证和语义检查。
 * <p>
 * <b>核心职责：</b>
 * <ul>
 * <li>验证映射结构的完整性（深度、字段数、表达式格式等）</li>
 * <li>检查表达式语法的合法性（基础检查，不执行）</li>
 * <li>提供友好的错误信息</li>
 * </ul>
 * <p>
 * <b>验证规则：</b>
 * <ul>
 * <li>MV-1: 节点深度不能超过限制</li>
 * <li>MV-2: 表达式不能包含明显的语法错误（如不匹配的括号）</li>
 * <li>MV-3: 循环表达式必须是有效的路径引用</li>
 * <li>MV-4: 对象字段名不能重复（已由 Map 保证）</li>
 * </ul>
 *
 * @author renc
 */
public class MappingSpecValidator {

    /**
     * 验证结果
     */
    public record ValidationResult(boolean isValid, List<String> errors) {
        public static ValidationResult success() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult failure(String error) {
            return new ValidationResult(false, List.of(error));
        }

        public static ValidationResult failure(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        /**
         * 获取第一个错误信息
         */
        public String firstError() {
            return errors.isEmpty() ? "" : errors.get(0);
        }
    }

    /**
     * 验证 MappingNode
     *
     * @param node 待验证的节点
     * @return 验证结果
     */
    public ValidationResult validate(MappingNode node) {
        if (node == null) {
            return ValidationResult.failure("MappingNode must not be null");
        }

        List<String> errors = new ArrayList<>();

        try {
            // 基础验证（由节点自身的 validate() 方法完成）
            node.validate();

            // 递归验证子节点
            validateRecursively(node, "", errors);

        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * 递归验证节点及其子节点
     *
     * @param node   当前节点
     * @param path   当前路径（用于错误定位）
     * @param errors 错误列表
     */
    private void validateRecursively(MappingNode node, String path, List<String> errors) {
        switch (node) {
            case ExpressionNode expr -> validateExpression(expr, path, errors);
            case ObjectNode obj -> validateObject(obj, path, errors);
            case ArrayNode arr -> validateArray(arr, path, errors);
        }
    }

    /**
     * 验证表达式节点
     */
    private void validateExpression(ExpressionNode node, String path, List<String> errors) {
        String expr = node.expression();

        // MV-2: 检查基础语法错误
        if (expr.contains("{{") || expr.contains("}}")) {
            long openCount = expr.chars().filter(c -> c == '{').count();
            long closeCount = expr.chars().filter(c -> c == '}').count();
            if (openCount != closeCount) {
                errors.add("Expression has unmatched braces at path: " + path + ", expr: " + expr);
            }
        }

        // 检查括号匹配
        if (!areParenthesesBalanced(expr)) {
            errors.add("Expression has unmatched parentheses at path: " + path + ", expr: " + expr);
        }
    }

    /**
     * 验证对象节点
     */
    private void validateObject(ObjectNode node, String path, List<String> errors) {
        for (var entry : node.fields().entrySet()) {
            String fieldName = entry.getKey();
            MappingNode fieldValue = entry.getValue();
            String fieldPath = path.isEmpty() ? fieldName : path + "." + fieldName;
            validateRecursively(fieldValue, fieldPath, errors);
        }
    }

    /**
     * 验证数组节点
     */
    private void validateArray(ArrayNode node, String path, List<String> errors) {
        if (node.isStatic()) {
            // 验证静态数组元素
            for (int i = 0; i < node.elements().size(); i++) {
                MappingNode element = node.elements().get(i);
                String elementPath = path + "[" + i + "]";
                validateRecursively(element, elementPath, errors);
            }
        } else if (node.isDynamic()) {
            // 验证动态数组模板
            String templatePath = path + "[template]";
            validateRecursively(node.itemTemplate(), templatePath, errors);

            // MV-3: 验证循环表达式
            String loopExpr = node.loopExpression();
            if (!isValidPathReference(loopExpr)) {
                errors.add("Invalid loop expression at path: " + path + ", expr: " + loopExpr);
            }
        }
    }

    /**
     * 检查括号是否平衡
     */
    private boolean areParenthesesBalanced(String expr) {
        int count = 0;
        for (char c : expr.toCharArray()) {
            if (c == '(')
                count++;
            else if (c == ')')
                count--;
            if (count < 0)
                return false;
        }
        return count == 0;
    }

    /**
     * 检查是否为有效的路径引用
     * <p>
     * 例如：#dsInput.users, #serviceInput.orderList
     */
    private boolean isValidPathReference(String expr) {
        if (expr == null || expr.isBlank()) {
            return false;
        }
        String trimmed = expr.trim();
        // 简单检查：开头是 # 或字母，包含 . 或字母数字
        return trimmed.matches("^[#a-zA-Z][a-zA-Z0-9._]*$");
    }
}
