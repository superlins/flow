package com.zwtech.flow.connector.factory.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.zwtech.flow.connector.Connector;
import com.zwtech.flow.connector.ConnectorAdapter;
import com.zwtech.flow.connector.DefaultExecutionEnvelope;
import com.zwtech.flow.connector.ExecutionEnvelope;
import com.zwtech.flow.connector.factory.ConnectorEndpointTypeNames;
import com.zwtech.flow.connector.filter.*;
import com.zwtech.flow.connector.specs.DatasourceSpecs;
import com.zwtech.flow.connector.specs.HttpDatasourceSpecs;
import com.zwtech.flow.connector.specs.SpecsConverter;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.core.VariableContext;
import com.zwtech.flow.core.plugin.SpringPluginManager;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.service.SchemaValidationService;
import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP Connector 适配器
 * <p>
 * 执行流程：
 * 1. 输入校验（使用 Datasource Contract）
 * 2. 转换为 DatasourceSpecs（规格层）
 * 3. 使用 RequestBinder 绑定为 HttpRequestSpec
 * 4. 通过 ConnectorFilterChain + Connector 执行
 * 5. 使用 ResponseConverter 转换响应
 * 6. 输出校验（使用 Datasource Contract）
 *
 * @author renc
 */
public class HttpConnectorAdapter implements ConnectorAdapter {

    private final SpringPluginManager pluginManager;
    private final ApplicationContext applicationContext;
    private final HttpConnectorFactory connectorFactory;
    private final SchemaValidationService schemaValidationService;
    private final HttpRequestBinder requestBinder;
    private final HttpResponseConverter responseConverter;

    public HttpConnectorAdapter(SpringPluginManager pluginManager,
                                 ApplicationContext applicationContext,
                                 HttpConnectorFactory connectorFactory,
                                 SchemaValidationService schemaValidationService) {
        this.pluginManager = pluginManager;
        this.applicationContext = applicationContext;
        this.connectorFactory = connectorFactory;
        this.requestBinder = new HttpRequestBinder();
        this.responseConverter = new HttpResponseConverter();
        this.schemaValidationService = schemaValidationService;
    }

    @Override
    public boolean supports(String type) {
        return ConnectorEndpointTypeNames.HTTP.equals(type);
    }

    @Override
    public Mono<ExecutionExchange> execute(ExecutionExchange exchange, ApiDatasource datasource) {
        // 1. 获取 Contract 并校验输入
        var contract = datasource.contract();
        JsonNode inputNode = exchange.getRequest();
        schemaValidationService.validate(contract.inputSchema(), inputNode);

        // 2. 转换为 DatasourceSpecs（规格层）
        DatasourceSpecs specs = SpecsConverter.toSpecs(datasource);
        HttpDatasourceSpecs httpSpecs = (HttpDatasourceSpecs) specs;

        // 3. 使用 RequestBinder 绑定为 HttpRequestSpec
        HttpRequestSpec requestSpec = requestBinder.bind(exchange, httpSpecs);

        // 4. 构建 ExecutionEnvelope
        ExecutionEnvelope<HttpRequestSpec, HttpResponseSpec> envelope = DefaultExecutionEnvelope.of(requestSpec);

        // 5. 创建 Connector（基于规格）
        Connector<HttpRequestSpec, HttpResponseSpec> connector = connectorFactory.create(specs);

        // 6. 构建 Filter 列表（全局 + 插件），按 Order 统一排序
        List<ConnectorFilter<HttpRequestSpec, HttpResponseSpec>> filters = buildFilters(datasource);

        // 7. 构建 ConnectorFilterChain
        ConnectorFilterChain chain = new DefaultConnectorFilterChain<>(connector, filters);

        // 8. 执行 Filter 链和 Connector
        return chain.filter(envelope).map(finalEnvelope -> {
            HttpResponseSpec responseSpec = finalEnvelope.responseSpec()
                    .orElseThrow(() -> new IllegalStateException("ResponseSpec is empty"));

            // 9. 使用 ResponseConverter 转换响应（直接转换）
            JsonNode responseData = responseConverter.convert(responseSpec);
            exchange.getAttributes().put("rawHttpResponse", responseSpec);

            // 10. 构建用于表达式解析的变量上下文
            JsonNode responseVar = responseConverter.toVariableFormat(responseSpec);
            VariableContext responseContext = exchange.getVariableContext().withResponse(responseVar);

            // 11. 使用 ResponseConverter 投影响应（字段映射）
            JsonNode outputNode = responseConverter.project(responseSpec, httpSpecs, responseContext);

            // 12. 输出校验
            schemaValidationService.validate(contract.outputSchema(), outputNode);

            // 13. 写回 ExecutionExchange
            ExecutionExchange resultExchange = exchange.mutate()
                    .response(outputNode)
                    .build();

            // 合并 envelope 的 attributes
            var attributes = finalEnvelope.attributes().toMap();
            resultExchange.getAttributes().putAll(attributes);

            return resultExchange;
        });
    }

    /**
     * 构建过滤器列表，合并 GlobalFilters 和 PF4J ConnectorFilters，并按 Order 统一排序
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<ConnectorFilter<HttpRequestSpec, HttpResponseSpec>> buildFilters(ApiDatasource datasource) {
        List<ConnectorFilter<HttpRequestSpec, HttpResponseSpec>> allFilters = new ArrayList<>();

        // 1. 添加 GlobalFilters（从 Spring ApplicationContext 获取）
        List<GlobalFilter> globalFilters = new ArrayList<>(
                applicationContext.getBeansOfType(GlobalFilter.class).values()
        );
        globalFilters.sort(OrderComparator.INSTANCE);

        // 将 GlobalFilters 包装成 ConnectorFilter
        for (GlobalFilter globalFilter : globalFilters) {
            int order = (globalFilter instanceof Ordered) ? ((Ordered) globalFilter).getOrder() : Ordered.LOWEST_PRECEDENCE;
            allFilters.add((ConnectorFilter<HttpRequestSpec, HttpResponseSpec>)
                    new GlobalFilterWrapper(globalFilter, order));
        }

        // 2. 添加 PF4J 插件 ConnectorFilters
        datasource.extensions().forEach(extension -> {
            var extensions = pluginManager.getExtensions(ConnectorFilter.class, extension.id());
            for (var connectorFilter : extensions) {
                allFilters.add((ConnectorFilter<HttpRequestSpec, HttpResponseSpec>) connectorFilter);
            }
        });

        // 3. 按 Order 统一排序所有过滤器
        allFilters.sort((f1, f2) -> {
            int order1 = getOrder(f1);
            int order2 = getOrder(f2);
            return Integer.compare(order1, order2);
        });

        return allFilters;
    }

    /**
     * 获取过滤器的 Order 值
     */
    private int getOrder(ConnectorFilter<?, ?> filter) {
        if (filter instanceof Ordered) {
            return ((Ordered) filter).getOrder();
        }
        return Ordered.LOWEST_PRECEDENCE;
    }
}
