package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.Objects;

public final class FieldBinding implements ValueObject<FieldBinding> {

    private final String targetField;
    private final String expression;

    public FieldBinding(String targetField, String expression) {
        Assert.hasText(targetField, "targetField must not be blank");
        Assert.hasText(expression, "expression must not be blank");
        this.targetField = targetField;
        this.expression = expression;
    }

    public String targetField() {
        return targetField;
    }

    public String expression() {
        return expression;
    }

    @Override
    public boolean sameValueAs(FieldBinding other) {
        return other != null
            && targetField.equals(other.targetField)
            && expression.equals(other.expression);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof FieldBinding that && sameValueAs(that));
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetField, expression);
    }
}