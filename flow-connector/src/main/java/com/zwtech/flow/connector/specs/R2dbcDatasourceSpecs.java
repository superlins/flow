package com.zwtech.flow.connector.specs;

import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.connection.R2DbcDatasourceConnection;
import com.zwtech.flow.domain.model.apidatasource.operation.SqlDatasourceOperation;

/**
 * R2DBC 数据源规格
 *
 * @author renc
 */
public final class R2dbcDatasourceSpecs implements DatasourceSpecs {

    private final R2DbcDatasourceConnection connection;
    private final SqlDatasourceOperation operation;

    public R2dbcDatasourceSpecs(
        R2DbcDatasourceConnection connection,
        SqlDatasourceOperation operation
    ) {
        this.connection = connection;
        this.operation = operation;
    }

    @Override
    public R2DbcDatasourceConnection getConnection() {
        return connection;
    }

    @Override
    public SqlDatasourceOperation getOperation() {
        return operation;
    }

    @Override
    public String getType() {
        return "r2dbc";
    }

    /**
     * 从 ApiDatasource 创建 R2dbcDatasourceSpecs
     */
    public static R2dbcDatasourceSpecs from(ApiDatasource datasource) {
        return new R2dbcDatasourceSpecs(
            (R2DbcDatasourceConnection) datasource.connection(),
            (SqlDatasourceOperation) datasource.operation()
        );
    }

    public static R2dbcDatasourceSpecs of(
        R2DbcDatasourceConnection connection,
        SqlDatasourceOperation operation
    ) {
        return new R2dbcDatasourceSpecs(connection, operation);
    }
}
