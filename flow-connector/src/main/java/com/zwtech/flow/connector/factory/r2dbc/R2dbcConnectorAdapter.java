package com.zwtech.flow.connector.factory.r2dbc;

import com.fasterxml.jackson.databind.JsonNode;
import com.zwtech.flow.connector.AbstractConnectorAdapter;
import com.zwtech.flow.connector.factory.ConnectorEndpointTypeNames;
import com.zwtech.flow.connector.factory.ConnectorFactory;
import com.zwtech.flow.connector.specs.DatasourceSpecs;
import com.zwtech.flow.connector.specs.R2dbcDatasourceSpecs;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.core.VariableContext;
import com.zwtech.flow.core.plugin.SpringPluginManager;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.service.SchemaValidationService;
import org.springframework.context.ApplicationContext;

/**
 * R2DBC Connector 适配器
 * <p>
 * 继承自 AbstractConnectorAdapter，声明 R2DBC 特定的类型和业务逻辑。
 * 基类统一处理 FilterChain 构建、Envelope 创建、响应转换流程。
 *
 * @author renc
 */
public class R2dbcConnectorAdapter
    extends AbstractConnectorAdapter<R2dbcRequestSpec, R2dbcResponseSpec, R2dbcDatasourceSpecs> {

    private final R2dbcConnectorFactory connectorFactory;
    private final R2dbcRequestBinder requestBinder;
    private final R2dbcResponseConverter responseConverter;

    public R2dbcConnectorAdapter(
            SpringPluginManager pluginManager,
            ApplicationContext applicationContext,
            R2dbcConnectorFactory connectorFactory,
            SchemaValidationService schemaValidationService) {
        super(pluginManager, applicationContext, schemaValidationService);
        this.connectorFactory = connectorFactory;
        this.requestBinder = new R2dbcRequestBinder();
        this.responseConverter = new R2dbcResponseConverter();
    }

    @Override
    protected String getSupportedType() {
        return ConnectorEndpointTypeNames.R2DBC;
    }

    @Override
    protected ConnectorFactory<R2dbcRequestSpec, R2dbcResponseSpec> getConnectorFactory() {
        return connectorFactory;
    }

    @Override
    protected DatasourceSpecs getSpecs(ApiDatasource datasource) {
        return R2dbcDatasourceSpecs.from(datasource);
    }

    @Override
    protected R2dbcDatasourceSpecs getTypedSpecs(DatasourceSpecs specs) {
        if (!(specs instanceof R2dbcDatasourceSpecs r2dbcSpecs)) {
            throw new IllegalArgumentException("Expected R2dbcDatasourceSpecs, got: " + specs.getClass().getName());
        }
        return r2dbcSpecs;
    }

    @Override
    protected R2dbcRequestSpec toRequest(ExecutionExchange exchange, R2dbcDatasourceSpecs specs) {
        // 使用 Binder 绑定请求
        return requestBinder.bind(exchange, specs);
    }

    @Override
    protected JsonNode convertResponse(R2dbcResponseSpec response, ExecutionExchange exchange, ApiDatasource datasource, R2dbcDatasourceSpecs specs) {
        // 构建用于表达式解析的变量上下文
        JsonNode responseVar = responseConverter.toVariableFormat(response);
        VariableContext responseContext = exchange.getVariableContext().withResponse(responseVar);

        // 字段映射
        return responseConverter.project(response, specs, responseContext);
    }
}