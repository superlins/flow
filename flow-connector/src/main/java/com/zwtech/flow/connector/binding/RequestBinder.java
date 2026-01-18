package com.zwtech.flow.connector.binding;

import com.zwtech.flow.connector.RequestSpec;
import com.zwtech.flow.connector.specs.DatasourceSpecs;
import com.zwtech.flow.core.ExecutionExchange;

/**
 * 请求绑定器接口，将 ExecutionExchange 转换为具体的 RequestSpec
 * <p>
 * 每个 Connector 类型需要实现自己的 RequestBinder。
 * 统一使用 VariableContext 解析模板表达式。
 *
 * @param <REQ extends RequestSpec> RequestSpec 类型
 * @author renc
 */
@FunctionalInterface
public interface RequestBinder<REQ extends RequestSpec, SPECS extends DatasourceSpecs> {

    /**
     * 将 ExecutionExchange 绑定为目标 Connector 的 RequestSpec
     *
     * @param exchange 执行交换，包含请求和变量上下文
     * @param specs    数据源规格，包含操作配置
     * @return RequestSpec
     */
    REQ bind(ExecutionExchange exchange, SPECS specs);
}
