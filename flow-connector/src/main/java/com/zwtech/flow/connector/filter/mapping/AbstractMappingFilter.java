package com.zwtech.flow.connector.filter.mapping;// package org.example.core.connector.filter.mapping;
//
// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.node.MissingNode;
// import com.fasterxml.jackson.databind.node.NullNode;
// import org.example.core.connector.filter.GlobalFilter;
// import org.example.core.expression.ExpressionEngine;
// import org.example.core.serviceregistry.MappingRule;
// import org.springframework.core.Ordered;
// import org.springframework.util.StringUtils;
//
// import java.util.Objects;
// import java.util.function.BiConsumer;
//
// /**
//  * @author renc
//  */
// public abstract class AbstractMappingFilter implements GlobalFilter, Ordered {
//
//     protected final ObjectMapper objectMapper;
//
//     protected final ExpressionEngine expressionEngine;
//
//     protected AbstractMappingFilter(ObjectMapper objectMapper, ExpressionEngine expressionEngine) {
//         this.objectMapper = objectMapper;
//         this.expressionEngine = expressionEngine;
//     }
//
//     protected void applyMappings(Iterable<MappingRule> rules, Object context, BiConsumer<MappingRule, Object> applyValue) {
//         rules.forEach(rule -> {
//             try {
//                 Object value = null;
//
//                 JsonNode rawValue = expressionEngine.evaluate(rule.getValueExpr(), context, JsonNode.class);
//
//                 // 处理 missingValueAction
//                 if (rawValue instanceof MissingNode) {
//                     switch (rule.getMissingValueAction()) {
//                         case IGNORE -> {
//                             return;
//                         }
//                         case SET_TO_DEFAULT -> value = getDefaultValue(rule, context);
//                         case SET_TO_NULL -> {
//                         }
//                         case THROW_EXCEPTION -> throw new IllegalArgumentException(getErrorMessage(rule, context));
//                     }
//                 }
//                 // 处理 nullValueAction
//                 else if (rawValue instanceof NullNode) {
//                     switch (rule.getNullValueAction()) {
//                         case IGNORE -> {
//                         }
//                         case SET_TO_DEFAULT -> value = getDefaultValue(rule, context);
//                         case THROW_EXCEPTION -> throw new IllegalArgumentException(getErrorMessage(rule, context));
//                         case REMOVE -> {
//                             return;
//                         }
//                     }
//                 }
//                 // 处理其他情况
//                 else {
//                     value = objectMapper.convertValue(rawValue, rule.getType());
//
//                     if (rule.getTransformExpr() != null) {
//                         value = expressionEngine.evaluate(rule.getTransformExpr(), value, rule.getType());
//                     }
//
//                     if (Objects.nonNull(rule.getInvalidValue()) || StringUtils.hasText(rule.getInvalidValueExpr())) {
//                         var invalidValue = getInvalidValue(rule, context);
//                         if (invalidValue != null && value != null && invalidValue.equals(value.toString())) {
//                             switch (rule.getInvalidValueAction()) {
//                                 case IGNORE, SET_TO_NULL -> {
//                                 }
//                                 case SET_TO_DEFAULT -> value = getDefaultValue(rule, context);
//                                 case THROW_EXCEPTION ->
//                                         throw new IllegalArgumentException(getErrorMessage(rule, context));
//                                 case REMOVE -> {
//                                     return;
//                                 }
//                             }
//                         }
//                     }
//                 }
//
//                 applyValue.accept(rule, value);
//             } catch (Exception e) {
//                 throw new RuntimeException("Error mapping '" + rule.getName() + "': " + e.getMessage(), e);
//             }
//         });
//     }
//
//     protected Object getInvalidValue(MappingRule mapping, Object context) {
//         if (Objects.nonNull(mapping.getInvalidValue())) {
//             return mapping.getInvalidValue();
//         } else {
//             return expressionEngine.evaluate(mapping.getInvalidValueExpr(), context, mapping.getType());
//         }
//     }
//
//     protected Object getDefaultValue(MappingRule mapping, Object context) {
//         if (Objects.nonNull(mapping.getDefaultValue())) {
//             return mapping.getDefaultValue();
//         } else {
//             return expressionEngine.evaluate(mapping.getDefaultValueExpr(), context, mapping.getType());
//         }
//     }
//
//     protected String getErrorMessage(MappingRule mapping, Object context) {
//         if (StringUtils.hasText(mapping.getExceptionMessage())) {
//             return mapping.getExceptionMessage();
//         } else if (StringUtils.hasText(mapping.getExceptionMessageExpr())) {
//             return expressionEngine.evaluate(mapping.getExceptionMessageExpr(), context, String.class);
//         } else {
//             return "Required parameter '" + mapping.getName() + "' is missing.";
//         }
//     }
// }
