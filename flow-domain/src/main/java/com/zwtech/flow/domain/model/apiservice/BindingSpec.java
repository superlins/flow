package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

/**
 * ApiService 契约中的字段，如何被“转换 / 推导 / 组合”为 ApiDatasource 契约所需要的字段
 *
 * - ApiDatasource#inputSchema
 * {
 *   "type": "object",
 *   "required": ["user_id", "mobile", "sign"],
 *   "properties": {
 *     "user_id": { "type": "string" },
 *     "mobile": { "type": "string" },
 *     "sign": { "type": "string" }
 *   }
 * }
 *
 * - ApiService#inputSchema
 * {
 *   "type": "object",
 *   "required": ["uid", "phone"],
 *   "properties": {
 *     "uid": { "type": "string" },
 *     "phone": { "type": "string" },
 *     "alg": {
 *       "type": "string",
 *       "enum": ["md5", "sha256"],
 *       "default": "md5"
 *     }
 *   }
 * }
 *
 * - ApiService#bindingSpec
 * {
 *   "input": [
 *     {
 *       "target": "user_id",
 *       "expression": "{{ $.uid }}"
 *     },
 *     {
 *       "target": "mobile",
 *       "expression": "{{ $.phone }}"
 *     },
 *     {
 *       "target": "sign",
 *       "expression": "{{ hash($.uid + $.phone, $.alg) }}"
 *     }
 *   ]
 * }
 *
 * - ApiService#outputSchema
 * {
 *   "type": "object",
 *   "properties": {
 *     "userScore": { "type": "integer" },
 *     "risk": { "type": "string" }
 *   }
 * }
 *
 * - ApiService#bindingSpec
 * {
 *   "output": [
 *     {
 *       "target": "userScore",
 *       "expression": "{{ $.score }}"
 *     },
 *     {
 *       "target": "risk",
 *       "expression": "{{ $.risk_level }}"
 *     }
 *   ]
 * }
 */
public final class BindingSpec implements ValueObject<BindingSpec> {

    private final List<FieldBinding> inputBindings;
    private final List<FieldBinding> outputBindings;

    public BindingSpec(List<FieldBinding> inputBindings,
                                List<FieldBinding> outputBindings) {
        Assert.notNull(inputBindings, "inputBindings must not be null");
        Assert.notNull(outputBindings, "outputBindings must not be null");
        this.inputBindings = List.copyOf(inputBindings);
        this.outputBindings = List.copyOf(outputBindings);
    }

    public List<FieldBinding> inputBindings() {
        return inputBindings;
    }

    public List<FieldBinding> outputBindings() {
        return outputBindings;
    }

    @Override
    public boolean sameValueAs(BindingSpec other) {
        return other != null
            && inputBindings.equals(other.inputBindings)
            && outputBindings.equals(other.outputBindings);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof BindingSpec that && sameValueAs(that));
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputBindings, outputBindings);
    }
}