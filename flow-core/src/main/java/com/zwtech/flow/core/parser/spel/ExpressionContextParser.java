package com.zwtech.flow.core.parser.spel;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;

/**
 * @author renc
 */
public class ExpressionContextParser extends SpelExpressionParser implements EmbeddedContextExpressionParser {

    private final EvaluationContext evaluationContext;

    public ExpressionContextParser() {
        this(new SpelParserConfiguration());
    }

    public ExpressionContextParser(SpelParserConfiguration configuration) {
        this(configuration, new EnhancedEvaluationContext());
    }

    public ExpressionContextParser(SpelParserConfiguration configuration, EvaluationContext evaluationContext) {
        super(configuration);
        Assert.notNull(configuration, "EvaluationContext must not be null");
        this.evaluationContext = evaluationContext;
    }

    @Override
    public Object parseValue(String expressionString) throws EvaluationException {
        return parseExpression(expressionString).getValue(evaluationContext);
    }

    @Override
    public <T> T parseValue(String expressionString, Class<T> desiredResultType) throws EvaluationException {
        return parseExpression(expressionString).getValue(evaluationContext, desiredResultType);
    }

    @Override
    public Object parseValue(String expressionString, Object rootObject) throws EvaluationException {
        return parseExpression(expressionString).getValue(evaluationContext, rootObject);
    }

    @Override
    public <T> T parseValue(String expressionString, Object rootObject, Class<T> desiredResultType) throws EvaluationException {
        return parseExpression(expressionString).getValue(evaluationContext, rootObject, desiredResultType);
    }

    @Override
    public Object parseValue(Expression expression) throws EvaluationException {
        return expression.getValue(evaluationContext);
    }

    @Override
    public <T> T parseValue(Expression expression, Class<T> desiredResultType) throws EvaluationException {
        return expression.getValue(evaluationContext, desiredResultType);
    }

    @Override
    public Object parseValue(Expression expression, Object rootObject) throws EvaluationException {
        return expression.getValue(evaluationContext, rootObject);
    }

    @Override
    public <T> T parseValue(Expression expression, Object rootObject, Class<T> desiredResultType) throws EvaluationException {
        return expression.getValue(evaluationContext, rootObject, desiredResultType);
    }
}
