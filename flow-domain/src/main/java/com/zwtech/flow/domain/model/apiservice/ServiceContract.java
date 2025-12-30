package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.shared.JsonSchema;
import com.zwtech.flow.domain.model.apidatasource.DatasourceContract;
import org.springframework.util.Assert;

/**
 * @author renc
 */
public final class ServiceContract {
    private final JsonSchema inputSchema;
    private final JsonSchema outputSchema;

    public ServiceContract(JsonSchema inputSchema, JsonSchema outputSchema) {
        Assert.notNull(inputSchema, "inputSchema must not be null");
        Assert.notNull(outputSchema, "outputSchema must not be null");
        this.inputSchema = inputSchema;
        this.outputSchema = outputSchema;
    }

    public JsonSchema inputSchema() {
        return inputSchema;
    }

    public JsonSchema outputSchema() {
        return outputSchema;
    }

    /**
     * 领域规则：
     * - ApiService 的输入必须能满足 Datasource 的输入要求
     * - BindingSpec 中引用的字段必须存在
     */
    public void assertCompatibleWith(DatasourceContract datasourceContract,
                                     BindingSpec bindingSpec) {

        Assert.notNull(datasourceContract, "datasourceContract must not be null");
        Assert.notNull(bindingSpec, "bindingSpec must not be null");

        inputSchema.assertSatisfy(
                datasourceContract.inputSchema(),
                bindingSpec
        );
    }
}
