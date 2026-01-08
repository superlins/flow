package com.zwtech.flow.connector.factory.http;

import com.zwtech.flow.connector.Connector;
import com.zwtech.flow.connector.ConnectorAdapter;
import com.zwtech.flow.connector.DefaultExecutionEnvelope;
import com.zwtech.flow.connector.ExecutionEnvelope;
import com.zwtech.flow.connector.factory.ConnectorEndpointTypeNames;
import com.zwtech.flow.connector.filter.ConnectorFilter;
import com.zwtech.flow.connector.filter.ConnectorFilterChain;
import com.zwtech.flow.connector.filter.DefaultConnectorFilterChain;
import com.zwtech.flow.core.DefaultVariableContext;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.core.plugin.SpringPluginManager;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.behavior.HttpOperationBehavior;
import com.zwtech.flow.domain.model.apidatasource.connection.HttpDatasourceConnection;
import com.zwtech.flow.domain.service.SchemaValidationService;
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
    public Mono<ExecutionExchange> execute(ExecutionExchange exchange, ApiDatasource datasource, String operationKey) {

        // 1. 获取指定的 Operation
        var operation = datasource.getOperation(operationKey);
        var contract = operation.getContract();
        var behavior = (HttpOperationBehavior) operation.getBehavior();

        // 2. 从 ExecutionExchange 取出 JsonNode
        JsonNode inputNode = exchange.getRequest();

        // 3. 按 Operation Contract 的 inputSchema 做运行期 JSON Schema 校验
        schemaValidationService.validate(contract.inputSchema(), inputNode);

        // 4. 获取 Connection 配置
        var connection = (HttpDatasourceConnection) datasource.connection();
        String baseUrl = connection != null ? connection.baseUrl() : null;

        // 5. 将输入绑定为 HttpRequestSpec（使用 HttpRequestBinder 解析模板表达式）
        var requestSpec = HttpRequestBinder.bind(exchange, behavior, baseUrl);

        // 6. 构建 ExecutionEnvelope（不可变）
        ExecutionEnvelope<HttpRequestSpec, HttpResponseSpec> envelope = DefaultExecutionEnvelope.of(requestSpec);

        // 7. 创建 Connector - 通过 datasource.connection 创建客户端实例
        Connector<HttpRequestSpec, HttpResponseSpec> connector = connectorFactory.create(datasource);

        // 8. 构建 Filter 列表（全局 + 插件）
        var filters = new LinkedList<ConnectorFilter<HttpRequestSpec, HttpResponseSpec>>();
        operation.getExtensions().forEach(extension -> {
            var extensions = pluginManager.getExtensions(ConnectorFilter.class, extension.id());
            for (var connectorFilter : extensions) {
                //noinspection unchecked
                filters.add((ConnectorFilter<HttpRequestSpec, HttpResponseSpec>) connectorFilter);
            }
        });

        // 9. 构建 ConnectorFilterChain
        ConnectorFilterChain chain = new DefaultConnectorFilterChain<>(connector, filters);

        // 10. 执行 Filter 链和 Connector
        return chain.filter(envelope).map(finalEnvelope -> {
            HttpResponseSpec respSpec = finalEnvelope.responseSpec()
                    .orElseThrow(() -> new IllegalStateException("ResponseSpec is empty"));

            exchange.getAttributes().put("rawHttpResponse", respSpec);

            // 11. 创建响应时的变量上下文（包含 $request 和 $response）
            var responseVariableContext = new DefaultVariableContext(
                    exchange.getRequest(),
                    convertResponseToJsonNode(respSpec)
            );

            // 12. 将响应结果投影到 Datasource.operation.responseBody 并返回 JsonNode
            final var outputNode = OBJECT_MAPPER.createObjectNode();
            if (behavior.responseBody() != null) {
                var templateParser = new com.zwtech.flow.core.parser.TemplateExpressionParser();
                behavior.responseBody().forEach((k, v) -> {
                    Object parsed = templateParser.parseObject(v, responseVariableContext);
                    if (parsed instanceof JsonNode) {
                        outputNode.set(k, (JsonNode) parsed);
                    } else if (parsed != null) {
                        outputNode.putPOJO(k, parsed);
                    }
                });
            } else {
                // 如果没有定义 responseBody 映射，直接使用原始响应
                // outputNode = respSpec.getBody() != null ? respSpec.getBody() : OBJECT_MAPPER.createObjectNode();
            }

            // 13. 按 Operation Contract 的 outputSchema 做运行期 JSON Schema 校验
            schemaValidationService.validate(contract.outputSchema(), outputNode);

            // 14. 写回 ExecutionExchange.response（不可变）
            var exchange1 = exchange.mutate().response(outputNode).build();
            var attributes = finalEnvelope.attributes().toMap();
            exchange1.getAttributes().putAll(attributes);

            return exchange1;
        });
    }

    /**
     * 将 HttpResponseSpec 转换为 JsonNode，用于表达式解析中的 $response 变量
     */
    private JsonNode convertResponseToJsonNode(HttpResponseSpec respSpec) {
        var responseNode = OBJECT_MAPPER.createObjectNode();
        if (respSpec.getStatusCode() != null) {
            responseNode.put("status", respSpec.getStatusCode().value());
        }
        if (respSpec.getBody() != null) {
            responseNode.set("body", respSpec.getBody());
        }
        if (respSpec.getHeaders() != null) {
            var headersNode = OBJECT_MAPPER.createObjectNode();
            respSpec.getHeaders().forEach((name, values) -> {
                if (values.size() == 1) {
                    headersNode.put(name, values.get(0));
                } else {
                    var arrayNode = OBJECT_MAPPER.createArrayNode();
                    values.forEach(arrayNode::add);
                    headersNode.set(name, arrayNode);
                }
            });
            responseNode.set("headers", headersNode);
        }
        return responseNode;
    }
}