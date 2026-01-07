package com.zwtech.flow.core.parser.spel;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.lang.Nullable;

/**
 * @author renc
 */
public interface EmbeddedContextExpressionParser extends ExpressionParser {

    @Nullable
    Object parseValue(String expressionString) throws EvaluationException;

    @Nullable
    <T> T parseValue(String expressionString, @Nullable Class<T> desiredResultType) throws EvaluationException;

    @Nullable
    Object parseValue(String expressionString, @Nullable Object rootObject) throws EvaluationException;

    @Nullable
    <T> T parseValue(String expressionString, @Nullable Object rootObject, @Nullable Class<T> desiredResultType)
            throws EvaluationException;

    @Nullable
    Object parseValue(Expression expression) throws EvaluationException;

    @Nullable
    <T> T parseValue(Expression expression, @Nullable Class<T> desiredResultType) throws EvaluationException;

    @Nullable
    Object parseValue(Expression expression, @Nullable Object rootObject) throws EvaluationException;

    @Nullable
    <T> T parseValue(Expression expression, @Nullable Object rootObject, @Nullable Class<T> desiredResultType)
            throws EvaluationException;
}
