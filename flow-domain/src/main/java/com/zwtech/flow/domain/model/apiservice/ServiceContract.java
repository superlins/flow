package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.shared.Contract;

/**
 * Service 契约
 * <p>
 * 继承自 {@link Contract}，定义 ApiService 面向客户端的输入输出约束。
 *
 * @author renc
 */
public final class ServiceContract extends Contract {

    public ServiceContract(String inputSchema, String outputSchema) {
        super(inputSchema, outputSchema);
    }

    /**
     * 创建 ServiceContract
     */
    public static ServiceContract of(String inputSchema, String outputSchema) {
        return new ServiceContract(inputSchema, outputSchema);
    }

    @Override
    public boolean sameValueAs(Contract other) {
        if (!super.sameValueAs(other)) {
            return false;
        }
        return other instanceof ServiceContract;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ServiceContract that = (ServiceContract) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "ServiceContract{}";
    }
}
