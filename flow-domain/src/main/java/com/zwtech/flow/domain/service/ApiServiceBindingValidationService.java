package com.zwtech.flow.domain.service;

import com.zwtech.flow.domain.model.apiservice.BindingSpec;
import com.zwtech.flow.domain.model.apiservice.ServiceContract;

/**
 * @author renc
 */
public interface ApiServiceBindingValidationService {

    // 校验 BindingSpec 本身是否是“自洽的”，能不能直接用 schema 扩展字段，不要 BindingSpec？
    // SB-1 targetField 必须存在于 ApiService input/output schema
    //
    // SB-2 同一个 targetField 不允许重复绑定
    //
    // SB-3 expression 不允许引用不存在的字段
    boolean validateBindingSpec(ServiceContract contract, BindingSpec bindingSpec);
}
