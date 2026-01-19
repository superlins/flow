package com.zwtech.flow.connector.specs;

import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.connection.HttpDatasourceConnection;
import com.zwtech.flow.domain.model.apidatasource.operation.HttpDatasourceOperation;

/**
 * HTTP 数据源规格
 *
 * @author renc
 */
public final class HttpDatasourceSpecs implements DatasourceSpecs {

    private final HttpDatasourceConnection connection;
    private final HttpDatasourceOperation operation;

    public HttpDatasourceSpecs(
        HttpDatasourceConnection connection,
        HttpDatasourceOperation operation
    ) {
        this.connection = connection;
        this.operation = operation;
    }

    @Override
    public HttpDatasourceConnection getConnection() {
        return connection;
    }

    @Override
    public HttpDatasourceOperation getOperation() {
        return operation;
    }

    @Override
    public String getType() {
        return "http";
    }

    /**
     * 从 ApiDatasource 创建 HttpDatasourceSpecs
     */
    public static HttpDatasourceSpecs from(ApiDatasource datasource) {
        return new HttpDatasourceSpecs(
            (HttpDatasourceConnection) datasource.connection(),
            (HttpDatasourceOperation) datasource.operation()
        );
    }

    public static HttpDatasourceSpecs of(
        HttpDatasourceConnection connection,
        HttpDatasourceOperation operation
    ) {
        return new HttpDatasourceSpecs(connection, operation);
    }
}
