package com.zwtech.flow.connector.factory.http;

import com.zwtech.flow.connector.Connector;
import com.zwtech.flow.connector.ConnectorAdapter;
import com.zwtech.flow.connector.DataBinder;
import com.zwtech.flow.connector.DefaultExecutionEnvelope;
import com.zwtech.flow.connector.ExecutionEnvelope;
import com.zwtech.flow.connector.factory.ConnectorEndpointTypeNames;
import com.zwtech.flow.connector.filter.ConnectorFilter;
import com.zwtech.flow.connector.filter.ConnectorFilterChain;
import com.zwtech.flow.connector.filter.DefaultConnectorFilterChain;
import com.zwtech.flow.core.DefaultExecutionContext;
import com.zwtech.flow.core.DefaultExecutionExchange;
import com.zwtech.flow.core.ExecutionContext;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.DatasourceContract;
import com.zwtech.flow.domain.service.SchemaValidationService;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

/**
 * HTTP Connector 适配器：
 *
 * 1. 从 ExecutionExchange.context.input 读取 JsonNode
 * 2. 按 ApiDatasource.contract.inputSchema 做 JSON Schema 校验
 * 3. 绑定为 HttpRequestSpec，构建 ExecutionEnvelope
 * 4. 通过 ConnectorFilterChain + Connector 发起调用
 * 5. 将 HttpResponseSpec 映射为 JsonNode，按 outputSchema 校验
 * 6. 写回 ExecutionExchange.context.output，返回新的 ExecutionExchange
 *
 * @author renc
 */
public class HttpConnectorAdapter implements ConnectorAdapter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpConnectorFactory connectorFactory;
    private final SchemaValidationService schemaValidationService;

    public HttpConnectorAdapter(HttpConnectorFactory connectorFactory,
                                SchemaValidationService schemaValidationService) {
        this.connectorFactory = connectorFactory;
        this.schemaValidationService = schemaValidationService;
    }

    public boolean supports(String type) {
        return ConnectorEndpointTypeNames.HTTP.equals(type);
    }

    @Override
    public Mono<ExecutionExchange> execute(ExecutionExchange exchange, ApiDatasource datasource) {

        // 1. 拿到 Datasource 契约
        DatasourceContract contract = datasource.contract();

        // 2. 从 ExecutionExchange.context.input 取出 JsonNode
        JsonNode inputNode = exchange.context().input()
                .orElseThrow(() -> new IllegalStateException("ExecutionContext.input is empty"));

        // 3. 按 Datasource.inputSchema 做运行期 JSON Schema 校验
        schemaValidationService.validate(contract.inputSchema(), inputNode);

        // 4. 将输入绑定为 HttpRequestSpec（这里用 DataBinder + ObjectMapper，后续可加强）
        HttpRequestSpec requestSpec = DataBinder.bind(inputNode, HttpRequestSpec.class);

        // 5. 构建 ExecutionEnvelope（不可变）
        ExecutionEnvelope<HttpRequestSpec, HttpResponseSpec> envelope =
                DefaultExecutionEnvelope.of(requestSpec);

        // 6. 创建 Connector
        Connector<HttpRequestSpec, HttpResponseSpec> connector = connectorFactory.create(datasource);

        // 7. 构建 Filter 列表（全局 + 插件），当前占位为空列表
        List<ConnectorFilter<HttpRequestSpec, HttpResponseSpec>> filters = List.of();
        // TODO: 根据 datasource.extensions() 用 pf4j 加载对应 ConnectorFilter，并合并全局过滤器

        // 8. 构建 ConnectorFilterChain
        ConnectorFilterChain chain = new DefaultConnectorFilterChain<>(connector, filters);

        // 9. 执行 Filter 链和 Connector
        return chain.filter(envelope)
                .map(finalEnvelope -> {
                    HttpResponseSpec respSpec = finalEnvelope.responseSpec()
                            .orElseThrow(() -> new IllegalStateException("ResponseSpec is empty"));

                    // 10. ResponseSpec -> JsonNode
                    JsonNode outputNode = OBJECT_MAPPER.convertValue(respSpec, JsonNode.class);

                    // 11. 按 Datasource.outputSchema 做运行期 JSON Schema 校验
                    schemaValidationService.validate(contract.outputSchema(), outputNode);

                    // 12. 写回 ExecutionExchange.context.output（不可变）
                    ExecutionContext oldCtx = exchange.context();
                    ExecutionContext newCtx;
                    if (oldCtx instanceof DefaultExecutionContext dec) {
                        newCtx = dec.withOutput(outputNode);
                    } else {
                        newCtx = new DefaultExecutionContext(
                                oldCtx.input(),
                                Optional.of(outputNode),
                                oldCtx.derived()
                        );
                    }

                    // 这里暂时不保留原 attributes，后续可以通过 Builder 模式从旧 exchange 复制 attributes
                    return new DefaultExecutionExchange(newCtx, null);
                });
    }
}