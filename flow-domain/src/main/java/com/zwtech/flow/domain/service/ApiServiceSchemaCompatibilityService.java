package com.zwtech.flow.domain.service;

import com.zwtech.flow.domain.model.apidatasource.OperationContract;
import com.zwtech.flow.domain.model.apiservice.BindingSpec;
import com.zwtech.flow.domain.model.apiservice.ServiceContract;

/**
 * @author renc
 */
public interface ApiServiceSchemaCompatibilityService {

    // 判断 ApiService 的契约 + 绑定规则，是否能够满足 ApiDatasource 的契约承诺
    // -> “这个 ApiService，真的能合法地调用这个 Datasource 吗？”
    // SC-1 Datasource inputSchema 的 required 字段
    // 必须能通过 ApiService inputSchema + BindingSpec 推导得到
    //
    // SC-2 字段类型必须兼容（不能 string → number）
    //
    // SC-3 strict = true 时
    // ApiService 不允许放宽约束（required / enum / format）
    //
    // SC-4 BindingSpec 中的 target
    // 必须存在于 ApiService Contract
    //
    // SC-5 BindingSpec 中的 source
    // 必须存在于 ApiService inputSchema

    // ApiDatasource ds = datasourceLookupService.getById(cmd.datasourceId());
    //
    // Assert.state(ds.isEnabled(), "Datasource must be ENABLED");
    //
    // serviceContractCompatibilityService.assertCompatible(
    //         cmd.serviceContract(),
    //         cmd.bindingSpec(),
    //         ds.contract()
    //         );
    //
    // ApiService service = ApiService.create(...);
    //
    // repository.save(service);

    void assertCompatible(ServiceContract serviceContract, BindingSpec bindingSpec, OperationContract operationContract);
}
