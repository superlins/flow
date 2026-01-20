package com.zwtech.flow.connector.specs;

import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.DatasourceType;
import com.zwtech.flow.domain.model.apidatasource.connection.DatasourceConnection;
import com.zwtech.flow.domain.model.apidatasource.operation.DatasourceOperation;

/**
 * 将领域模型转换为执行规格
 * <p>
 * 已弃用：请使用 {@link DatasourceSpecsRegistry} 替代。
 * DatasourceSpecsRegistry 基于 SPI 机制，支持通过 Spring DI 扩展新的数据源类型。
 *
 * @author renc
 * @deprecated 使用 {@link DatasourceSpecsRegistry#createSpecs(ApiDatasource)} 替代
 */
@Deprecated(since = "0.0.1", forRemoval = true)
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
            case HTTP -> new HttpDatasourceSpecs(
                    (com.zwtech.flow.domain.model.apidatasource.connection.HttpDatasourceConnection) connection,
                    (com.zwtech.flow.domain.model.apidatasource.operation.HttpDatasourceOperation) operation);
            case R2DBC -> new R2dbcDatasourceSpecs(
                    (com.zwtech.flow.domain.model.apidatasource.connection.R2DbcDatasourceConnection) connection,
                    (com.zwtech.flow.domain.model.apidatasource.operation.SqlDatasourceOperation) operation);
            default -> throw new IllegalArgumentException("Unsupported datasource type: " + datasource.type());
        };
    }
}