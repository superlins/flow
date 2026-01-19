package com.zwtech.flow.connector.specs;

import com.zwtech.flow.domain.model.apidatasource.connection.DatasourceConnection;
import com.zwtech.flow.domain.model.apidatasource.operation.DatasourceOperation;

/**
 * 数据源规格接口
 * <p>
 * 承载数据源执行所需的全部配置信息，为可序列化、可传递的规格对象。
 * 不参与领域业务逻辑，只提供执行配置。
 *
 * @author renc
 */
public interface DatasourceSpecs {

    /**
     * 获取连接配置
     * ConnectorFactory 使用此信息构建连接客户端（WebClient、DatabaseClient 等）
     */
    DatasourceConnection getConnection();

    /**
     * 获取操作配置
     * 定义执行的具体行为（HTTP method/path、SQL、CQL 等）
     */
    DatasourceOperation getOperation();

    /**
     * 获取数据源类型标识
     */
    String getType();
}
