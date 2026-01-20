package com.zwtech.flow.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zwtech.flow.connector.factory.ConnectorFactory;
import com.zwtech.flow.connector.filter.*;
import com.zwtech.flow.connector.specs.DatasourceSpecs;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.core.plugin.SpringPluginManager;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.service.SchemaValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Connector Adapter
 * <p>
 * 使用模板方法模式，定义 Connector 执行的标准流程。
 * 通过泛型约束和抽象方法，由子类声明具体类型，但基类统一处理执行流程。
 * <p>
 * 核心数据流：
 * <ul>
 * <li>运行时数据：ExecutionExchange.getRequest()</li>
 * <li>静态配置：ApiDatasource (connection, operation, mappings)</li>
 * <li>执行规格：SPECS (connection, operation)</li>
 * <li>执行请求：REQ (基于运行时 + 静态配置构建)</li>
 * <li>执行响应：RESP (原始响应数据)</li>
 * <li>输出数据：ExecutionExchange.getResponse() (基于 RESP + mappings 提取)</li>
 * </ul>
 * <p>
 * 标准执行流程：
 * <ol>
 * <li>类型转换和请求绑定（调用 toRequest 钩子 - 需要运行时 + 静态配置）</li>
 * <li>输入校验（基类统一处理）</li>
 * <li>构建 FilterChain 和 Envelope（基类统一处理）</li>
 * <li>执行 Connector + Filter（基类统一处理）</li>
 * <li>响应转换和字段映射（调用 convertResponse 钩子 - 需要 RESP + mappings 提取）</li>
 * <li>输出校验（基类统一处理）</li>
 * </ol>
 *
 * @param <REQ   extends RequestSpec> RequestSpec 类型
 * @param <RESP  extends ResponseSpec> ResponseSpec 类型
 * @param <SPECS extends DatasourceSpecs> DatasourceSpecs 类型
 * @author renc
 */
public abstract class AbstractConnectorAdapter<REQ extends RequestSpec, RESP extends ResponseSpec, SPECS extends DatasourceSpecs>
        implements ConnectorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConnectorAdapter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected final SpringPluginManager pluginManager;
    protected final ApplicationContext applicationContext;
    protected final SchemaValidationService schemaValidationService;

    public AbstractConnectorAdapter(
            SpringPluginManager pluginManager,
            ApplicationContext applicationContext,
            SchemaValidationService schemaValidationService) {
        this.pluginManager = pluginManager;
        this.applicationContext = applicationContext;
        this.schemaValidationService = schemaValidationService;
    }

    @Override
    public final boolean supports(String type) {
        return getSupportedType().equals(type);
    }

    @Override
    public final Mono<ExecutionExchange> execute(ExecutionExchange exchange, ApiDatasource datasource) {
        var contract = datasource.contract();
        JsonNode inputNode = exchange.getRequest();

        // 1. 类型转换和请求绑定：调用子类的 toRequest 钩子
        DatasourceSpecs specs = getSpecs(datasource);
        SPECS typedSpecs = getTypedSpecs(specs);
        REQ request = toRequest(exchange, typedSpecs);

        // 2. 输入校验
        schemaValidationService.validate(contract.inputSchema(), inputNode);

        // 3. 创建 Connector（使用 getConnectorFactory 约束）
        Connector<REQ, RESP> connector = getConnectorFactory().create(typedSpecs);

        // 4. 构建 FilterChain（基类统一处理）
        List<ConnectorFilter<REQ, RESP>> filters = buildFilters(datasource);
        ConnectorFilterChain chain = new DefaultConnectorFilterChain<>(connector, filters);

        // 5. 构建 Envelope（基类统一处理）
        ExecutionEnvelope<REQ, RESP> envelope = buildEnvelope(request);

        // 6. 执行
        return chain.filter(envelope)
                .map(finalEnvelope -> {
                    RESP response = finalEnvelope.responseSpec()
                            .orElseThrow(() -> new IllegalStateException("ResponseSpec is empty"));

                    // 7. 保存原始响应
                    exchange.getAttributes().put("rawResponseSpec", response);

                    // 8. 响应转换和字段映射：调用子类的 convertResponse 钩子
                    JsonNode output = convertResponse(response, exchange, datasource, typedSpecs);

                    // 9. 输出校验
                    schemaValidationService.validate(contract.outputSchema(), output);

                    return exchange.mutate().response(output).build();
                })
                .onErrorResume(ex -> {
                    LOGGER.error("Connector execution error for datasource: {}", datasource.id(), ex);
                    return Mono.error(ex);
                });
    }

    /**
     * 子类必须实现：返回支持的数据源类型
     */
    protected abstract String getSupportedType();

    /**
     * 子类必须实现：提供对应的 ConnectorFactory
     * 确保每个 ConnectorAdapter 都有对应的 ConnectorFactory
     */
    protected abstract ConnectorFactory<REQ, RESP> getConnectorFactory();

    /**
     * 子类必须实现：执行请求绑定
     * 子类负责：
     * - 从 ExecutionExchange.getRequest() 获取运行时数据
     * - 从 ApiDatasource 获取静态配置（connection, operation, inputMappings）
     * - 使用 SPECS (connection, operation) 构建 RequestSpec
     * - 应用 inputMappings 进行字段映射/转换
     */
    protected abstract REQ toRequest(ExecutionExchange exchange, SPECS specs);

    /**
     * 子类必须实现：将 DatasourceSpecs 转换为 SPECS
     */
    protected abstract SPECS getTypedSpecs(DatasourceSpecs specs);

    /**
     * 子类可选重写：响应转换和字段映射
     * 子类负责：
     * - 接收 RESP 原始响应数据
     * - 从 ApiDatasource 获取 outputMappings
     * - 使用 outputMappings 提取/映射响应字段
     */
    protected JsonNode convertResponse(RESP response, ExecutionExchange exchange, ApiDatasource datasource,
            SPECS specs) {
        // 默认实现：简单的序列化
        return OBJECT_MAPPER.valueToTree(response);
    }

    /**
     * 子类可选重写：自定义 Envelope 构建
     * 默认使用 DefaultExecutionEnvelope
     */
    protected ExecutionEnvelope<REQ, RESP> buildEnvelope(REQ request) {
        return DefaultExecutionEnvelope.of(request);
    }

    /**
     * 子类可选重写：从 ApiDatasource 获取 DatasourceSpecs
     * <p>
     * 推荐子类直接调用具体的 Specs.from(datasource) 方法，
     * 或注入 {@link com.zwtech.flow.connector.specs.DatasourceSpecsRegistry} 使用 SPI
     * 机制。
     * <p>
     * 默认实现使用已弃用的 SpecsConverter（将在未来版本移除）。
     */
    @SuppressWarnings("deprecation")
    protected DatasourceSpecs getSpecs(ApiDatasource datasource) {
        return com.zwtech.flow.connector.specs.SpecsConverter.toSpecs(datasource);
    }

    /**
     * 构建过滤器列表（基类统一处理）
     */
    protected List<ConnectorFilter<REQ, RESP>> buildFilters(ApiDatasource datasource) {
        List<ConnectorFilter<REQ, RESP>> allFilters = new ArrayList<>();

        // 1. 添加 GlobalFilters
        List<GlobalFilter> globalFilters = new ArrayList<>(
                applicationContext.getBeansOfType(GlobalFilter.class).values());
        globalFilters.sort(OrderComparator.INSTANCE);

        for (GlobalFilter globalFilter : globalFilters) {
            int order = (globalFilter instanceof Ordered)
                    ? ((Ordered) globalFilter).getOrder()
                    : Ordered.LOWEST_PRECEDENCE;
            allFilters.add(new GlobalFilterWrapper<>(globalFilter, order));
        }

        // 2. 添加 PF4J 插件 ConnectorFilters
        datasource.extensions().forEach(extension -> {
            var extensions = pluginManager.getExtensions(ConnectorFilter.class, extension.id());
            extensions.forEach(filter -> allFilters.add((ConnectorFilter<REQ, RESP>) filter));
        });

        // 3. 排序
        allFilters.sort((f1, f2) -> {
            int order1 = getOrder(f1);
            int order2 = getOrder(f2);
            return Integer.compare(order1, order2);
        });

        return allFilters;
    }

    private int getOrder(ConnectorFilter<?, ?> filter) {
        if (filter instanceof Ordered) {
            return ((Ordered) filter).getOrder();
        }
        return Ordered.LOWEST_PRECEDENCE;
    }
}
