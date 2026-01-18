package com.zwtech.flow.connector.specs;

import com.zwtech.flow.domain.model.apidatasource.connection.R2DbcDatasourceConnection;
import com.zwtech.flow.domain.model.apidatasource.operation.SqlDatasourceOperation;

import java.util.Map;

/**
 * R2DBC 数据源规格
 *
 * @author renc
 */
public final class R2dbcDatasourceSpecs implements DatasourceSpecs {

    private final R2DbcDatasourceConnection connection;
    private final SqlDatasourceOperation operation;
    private final Map<String, String> inputMappings;
    private final Map<String, String> outputMappings;

    public R2dbcDatasourceSpecs(
        R2DbcDatasourceConnection connection,
        SqlDatasourceOperation operation,
        Map<String, String> inputMappings,
        Map<String, String> outputMappings
    ) {
        this.connection = connection;
        this.operation = operation;
        this.inputMappings = inputMappings != null ? Map.copyOf(inputMappings) : Map.of();
        this.outputMappings = outputMappings != null ? Map.copyOf(outputMappings) : Map.of();
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
    public Map<String, String> getInputMappings() {
        return inputMappings;
    }

    @Override
    public Map<String, String> getOutputMappings() {
        return outputMappings;
    }

    @Override
    public String getType() {
        return "r2dbc";
    }

    public static R2dbcDatasourceSpecs of(
        R2DbcDatasourceConnection connection,
        SqlDatasourceOperation operation
    ) {
        return new R2dbcDatasourceSpecs(connection, operation, Map.of(), Map.of());
    }
}
