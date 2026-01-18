package com.zwtech.flow.connector.specs;

import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.connection.DatasourceConnection;
import com.zwtech.flow.domain.model.apidatasource.operation.DatasourceOperation;

/**
 * 将领域模型转换为执行规格
 *
 * @author renc
 */
public final class SpecsConverter {

    private SpecsConverter() {
    }

    /**
     * 将 ApiDatasource 转换为 DatasourceSpecs
     */
    public static DatasourceSpecs toSpecs(ApiDatasource datasource) {
        DatasourceConnection connection = datasource.connection();
        DatasourceOperation operation = datasource.operation();

        return switch (datasource.type()) {
            case "http" -> new HttpDatasourceSpecs(
                (com.zwtech.flow.domain.model.apidatasource.connection.HttpDatasourceConnection) connection,
                (com.zwtech.flow.domain.model.apidatasource.operation.HttpDatasourceOperation) operation,
                datasource.requestBodyMappings(),
                datasource.responseBodyMappings()
            );
            case "r2dbc" -> new R2dbcDatasourceSpecs(
                (com.zwtech.flow.domain.model.apidatasource.connection.R2DbcDatasourceConnection) connection,
                (com.zwtech.flow.domain.model.apidatasource.operation.SqlDatasourceOperation) operation,
                datasource.requestBodyMappings(),
                datasource.responseBodyMappings()
            );
            default -> throw new IllegalArgumentException("Unsupported datasource type: " + datasource.type());
        };
    }
}
