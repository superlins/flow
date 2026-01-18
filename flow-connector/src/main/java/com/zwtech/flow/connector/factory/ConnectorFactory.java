package com.zwtech.flow.connector.factory;

import com.zwtech.flow.connector.Connector;
import com.zwtech.flow.connector.RequestSpec;
import com.zwtech.flow.connector.ResponseSpec;
import com.zwtech.flow.connector.specs.DatasourceSpecs;

/**
 * 连接器工厂接口
 * <p>
 * 根据数据源规格创建 Connector 实例。
 * Connection 信息用于构建连接客户端（WebClient、DatabaseClient 等）。
 *
 * @param <REQ extends RequestSpec> RequestSpec 类型
 * @param <RESP extends ResponseSpec> ResponseSpec 类型
 * @author renc
 */
public interface ConnectorFactory<REQ extends RequestSpec, RESP extends ResponseSpec> {

    /**
     * 根据数据源规格创建 Connector
     *
     * @param specs 数据源规格，包含连接配置
     * @return Connector 实例
     */
    Connector<REQ, RESP> create(DatasourceSpecs specs);
}
