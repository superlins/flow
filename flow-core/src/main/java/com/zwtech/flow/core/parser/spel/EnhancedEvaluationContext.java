package com.zwtech.flow.core.parser.spel;

import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Arrays;

/**
 * @author renc
 */
public class EnhancedEvaluationContext extends StandardEvaluationContext {

    public EnhancedEvaluationContext() {
        Arrays.stream(Functions.values()).forEach(f -> registerFunction(f.n(), f.h()));
    }
}
