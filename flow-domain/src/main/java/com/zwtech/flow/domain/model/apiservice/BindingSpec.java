package com.zwtech.flow.domain.model.apiservice;

import java.util.Map;

/**
 * 绑定规范
 * 用于Workflow模式的Service配置
 */
public final class BindingSpec {

    private final Map<String, Object> inputBinding;
    private final Map<String, Object> outputBinding;

    public BindingSpec(Map<String, Object> inputBinding, Map<String, Object> outputBinding) {
        this.inputBinding = inputBinding != null ? Map.copyOf(inputBinding) : Map.of();
        this.outputBinding = outputBinding != null ? Map.copyOf(outputBinding) : Map.of();
    }

    public Map<String, Object> inputBinding() {
        return Map.copyOf(inputBinding);
    }

    public Map<String, Object> outputBinding() {
        return Map.copyOf(outputBinding);
    }
}
