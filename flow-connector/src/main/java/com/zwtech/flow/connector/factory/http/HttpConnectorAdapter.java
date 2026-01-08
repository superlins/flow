package com.zwtech.flow.connector.factory.http;

import com.zwtech.flow.connector.Connector;
import com.zwtech.flow.connector.ConnectorAdapter;
import com.zwtech.flow.connector.DefaultExecutionEnvelope;
import com.zwtech.flow.connector.ExecutionEnvelope;
import com.zwtech.flow.connector.factory.ConnectorEndpointTypeNames;
import com.zwtech.flow.connector.filter.ConnectorFilter;
import com.zwtech.flow.connector.filter.ConnectorFilterChain;
import com.zwtech.flow.connector.filter.DefaultConnectorFilterChain;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.core.plugin.SpringPluginManager;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.behavior.HttpOperationBehavior;
import com.zwtech.flow.domain.service.SchemaValidationService;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedList;

/**
 * HTTP Connector 适配器：
 * <p>
 * 1. 从 ExecutionExchange.request 读取 JsonNode
 * 2. 按 ApiDatasource.contract.inputSchema 做 JSON Schema 校验
 * 3. 绑定为 HttpRequestSpec，构建 ExecutionEnvelope
 * 4. 通过 ConnectorFilterChain + Connector 发起调用
 * 5. 将 HttpResponseSpec 映射为 JsonNode，按 outputSchema 校验
 * 6. 写回 ExecutionExchange.response，返回新的 ExecutionExchange
 *
 * @author renc
 */
public class HttpConnectorAdapter implements ConnectorAdapter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final SpringPluginManager pluginManager;

    private final HttpConnectorFactory connectorFactory;
    private final SchemaValidationService schemaValidationService;

    public HttpConnectorAdapter(SpringPluginManager pluginManager, HttpConnectorFactory connectorFactory, SchemaValidationService schemaValidationService) {
        this.pluginManager = pluginManager;
        this.connectorFactory = connectorFactory;
        this.schemaValidationService = schemaValidationService;
    }

    public boolean supports(String type) {
        return ConnectorEndpointTypeNames.HTTP.equals(type);
    }

    @Override
    public Mono<ExecutionExchange> execute(ExecutionExchange exchange, ApiDatasource datasource) {

        // 1. 拿到 Datasource 契约
        var contract = datasource.contract();
        var operation = (HttpOperationBehavior) datasource.operation();

        // 2. 从 ExecutionExchange.context.input 取出 JsonNode
        JsonNode inputNode = exchange.getRequest();

        // 3. 按 Datasource.inputSchema 做运行期 JSON Schema 校验
        schemaValidationService.validate(contract.inputSchema(), inputNode);

        // 3.1 TODO 根据 contract.inputSchema() 解析当前 http 请求的 body/headers/params/pathVariables

        var httpHeaders = new HttpHeaders();
        operation.headers().forEach(httpHeaders::add);

        // 4. 将输入绑定为 HttpRequestSpec TODO(renc): 根据 datasource 的 operation、connection、contract 结合 inputNode 来构建 HttpRequestSpec
        var requestSpec = HttpRequestSpec.builder()
                .url(operation.url())
                .method(HttpMethod.valueOf(operation.method()))
                .headers(httpHeaders)
                .body(operation.requestBody())
                .queryParams(operation.queryParams())
                .timeout(operation.timeout())
                .retries(0)
                .build();

        // 5. 构建 ExecutionEnvelope（不可变）
        ExecutionEnvelope<HttpRequestSpec, HttpResponseSpec> envelope = DefaultExecutionEnvelope.of(requestSpec);

        // 6. 创建 Connector - 通过 datasource.connection 创建客户端实例
        Connector<HttpRequestSpec, HttpResponseSpec> connector = connectorFactory.create(datasource);

        // 7. 构建 Filter 列表（全局 + 插件）
        var filters = new LinkedList<ConnectorFilter<HttpRequestSpec, HttpResponseSpec>>();
        datasource.extensions().forEach(extension -> {
            var extensions = pluginManager.getExtensions(ConnectorFilter.class, extension.id());
            for (var connectorFilter : extensions) {
                //noinspection unchecked
                filters.add((ConnectorFilter<HttpRequestSpec, HttpResponseSpec>) connectorFilter);
            }
        });

        // 8. 构建 ConnectorFilterChain
        ConnectorFilterChain chain = new DefaultConnectorFilterChain<>(connector, filters);

        // 9. 执行 Filter 链和 Connector
        return chain.filter(envelope).map(finalEnvelope -> {
            HttpResponseSpec respSpec = finalEnvelope.responseSpec()
                    .orElseThrow(() -> new IllegalStateException("ResponseSpec is empty"));

            exchange.getAttributes().put("rawHttpResponse", respSpec);

            // 10. 将响应结果投影到 Datasource.operation.responseBody 并返回 JsonNode
            var outputNode = OBJECT_MAPPER.createObjectNode();
            operation.responseBody().forEach((k, v) -> {
                // TODO 这里需要完成表达式解析，表达式可能引用请求对象 `$request` 也可能引用的是响应对象 `$response`
                outputNode.put(k, parseExpression(v).getValue(respSpec, String.class));
            });

            // 11. 按 Datasource.outputSchema 做运行期 JSON Schema 校验
            schemaValidationService.validate(contract.outputSchema(), outputNode);

            // 12. 写回 ExecutionExchange.context.output（不可变）
            var exchange1 = exchange.mutate().response(outputNode).build();
            var attributes = finalEnvelope.attributes().toMap();
            exchange1.getAttributes().putAll(attributes);

            return exchange1;
        });
    }

    private Expression parseExpression(Object v) {
        return new LiteralExpression("1");
    }
}