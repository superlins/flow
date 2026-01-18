package com.zwtech.flow.connector.specs;

import com.zwtech.flow.domain.model.apidatasource.connection.HttpDatasourceConnection;
import com.zwtech.flow.domain.model.apidatasource.operation.HttpDatasourceOperation;

import java.util.Map;

/**
 * HTTP 数据源规格
 *
 * @author renc
 */
public final class HttpDatasourceSpecs implements DatasourceSpecs {

    private final HttpDatasourceConnection connection;
    private final HttpDatasourceOperation operation;
    private final Map<String, String> inputMappings;
    private final Map<String, String> outputMappings;

    public HttpDatasourceSpecs(
        HttpDatasourceConnection connection,
        HttpDatasourceOperation operation,
        Map<String, String> inputMappings,
        Map<String, String> outputMappings
    ) {
        this.connection = connection;
        this.operation = operation;
        this.inputMappings = inputMappings != null ? Map.copyOf(inputMappings) : Map.of();
        this.outputMappings = outputMappings != null ? Map.copyOf(outputMappings) : Map.of();
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
    public Map<String, String> getInputMappings() {
        return inputMappings;
    }

    @Override
    public Map<String, String> getOutputMappings() {
        return outputMappings;
    }

    @Override
    public String getType() {
        return "http";
    }

    public static HttpDatasourceSpecs of(
        HttpDatasourceConnection connection,
        HttpDatasourceOperation operation
    ) {
        return new HttpDatasourceSpecs(connection, operation, Map.of(), Map.of());
    }
}
