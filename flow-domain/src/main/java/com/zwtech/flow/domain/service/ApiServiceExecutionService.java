package com.zwtech.flow.domain.service;

import com.zwtech.flow.connector.ConnectorAdapter;
import com.zwtech.flow.connector.factory.ConnectorAdapterFactory;
import com.zwtech.flow.core.DefaultVariableContext;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.core.VariableContext;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.DatasourceNotEnabledException;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasourceRepository;
import com.zwtech.flow.domain.model.apiservice.ApiService;
import com.zwtech.flow.domain.model.apiservice.ServiceNotEnabledException;
import com.zwtech.flow.domain.model.apiservice.ApiServiceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * ApiServiceExecutionService
 * 
 * 实现从 ApiService 到 ApiDatasource 的完整执行链路：
 * 
 * Phase 1: ApiService 入口
 *   - 接收 serviceInput（需过 ApiService Input Schema 校验）
 * 
 * Phase 2: Mapping (Input)
 *   - 上下文可见：#serviceInput, #env
 *   - 产出：#dsInput
 * 
 * Phase 3: Datasource 执行
 *   - 输入：#dsInput
 *   - 适配不同 connector 的请求 RequestSpec 和响应 ResponseSpec 对象：#req, #resp
 *   - 输出：#dsOutput
 * 
 * Phase 4: Mapping (Output)
 *   - 上下文可见：#serviceInput, #dsInput, #dsOutput, #req, #resp
 *   - 产出：#serviceOutput
 *
 * @author renc
 */
@Service
public class ApiServiceExecutionService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ApiServiceRepository apiServiceRepository;
    private final ApiDatasourceRepository apiDatasourceRepository;
    private final InputMappingService inputMappingService;
    private final OutputMappingService outputMappingService;
    private final SchemaValidationService schemaValidationService;
    private final ConnectorAdapterFactory connectorAdapterFactory;

    public ApiServiceExecutionService(
            ApiServiceRepository apiServiceRepository,
            ApiDatasourceRepository apiDatasourceRepository,
            InputMappingService inputMappingService,
            OutputMappingService outputMappingService,
            SchemaValidationService schemaValidationService,
            ConnectorAdapterFactory connectorAdapterFactory) {
        this.apiServiceRepository = apiServiceRepository;
        this.apiDatasourceRepository = apiDatasourceRepository;
        this.inputMappingService = inputMappingService;
        this.outputMappingService = outputMappingService;
        this.schemaValidationService = schemaValidationService;
        this.connectorAdapterFactory = connectorAdapterFactory;
    }

    /**
     * 执行 ApiService
     * 
     * @param serviceId ApiService 标识
     * @param serviceInput ApiService 的输入（已通过 ServiceContract.inputSchema 校验）
     * @param env 环境变量（可选）
     * @return ApiService 的输出（符合 ServiceContract.outputSchema）
     */
    public Mono<JsonNode> execute(String serviceId, JsonNode serviceInput, JsonNode env) {
        // Phase 1: 加载 ApiService 并验证状态
        return apiServiceRepository.find(new com.zwtech.flow.domain.model.apiservice.ServiceId(serviceId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("ApiService not found: " + serviceId)))
                .flatMap(service -> {
                    // 验证服务状态
                    if (!service.isEnabled()) {
                        return Mono.error(new ServiceNotEnabledException(service.id()));
                    }

                    // Phase 2: 加载 ApiDatasource
                    return apiDatasourceRepository.findById(service.datasourceId())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                    "ApiDatasource not found: " + service.datasourceId())))
                            .flatMap(datasource -> {
                                // 验证 Datasource 状态
                                if (!datasource.isEnabled()) {
                                    return Mono.error(new DatasourceNotEnabledException(datasource.id()));
                                }

                                // 验证 Datasource 是否已配置
                                if (!datasource.isConfigured()) {
                                    return Mono.error(new IllegalStateException(
                                            "Datasource '" + datasource.id() + "' is not configured"));
                                }

                                // Phase 3: Input Mapping
                                JsonNode dsInput = inputMappingService.mapInput(
                                        serviceInput, 
                                        service.mapping(), 
                                        env);

                                // 验证 dsInput 是否符合 DatasourceContract.inputSchema
                                schemaValidationService.validate(
                                        datasource.contract().inputSchema(), 
                                        dsInput);

                                // Phase 4: 创建 ExecutionExchange
                                VariableContext variableContext = new DefaultVariableContext(
                                        serviceInput, dsInput, null, null, null);
                                ExecutionExchange exchange = new com.zwtech.flow.core.DefaultExecutionExchange(
                                        dsInput,
                                        OBJECT_MAPPER.createObjectNode(),
                                        variableContext);

                                // Phase 5: 执行 Datasource（通过 ConnectorAdapter）
                                ConnectorAdapter adapter = connectorAdapterFactory.createAdapter(datasource.type());
                                return adapter.execute(exchange, datasource)
                                        .flatMap(dsExchange -> {
                                            // Phase 6: 获取 Datasource 输出
                                            JsonNode dsOutput = dsExchange.getResponse();

                                            // 验证 dsOutput 是否符合 DatasourceContract.outputSchema
                                            schemaValidationService.validate(
                                                    datasource.contract().outputSchema(), 
                                                    dsOutput);

                                            // Phase 7: Output Mapping
                                            Object req = dsExchange.getAttribute("rawHttpRequest");
                                            Object resp = dsExchange.getAttribute("rawHttpResponse");
                                            
                                            JsonNode serviceOutput = outputMappingService.mapOutput(
                                                    serviceInput,
                                                    dsInput,
                                                    dsOutput,
                                                    req,
                                                    resp,
                                                    service.mapping());

                                            // 验证 serviceOutput 是否符合 ServiceContract.outputSchema
                                            schemaValidationService.validate(
                                                    service.contract().outputSchema(), 
                                                    serviceOutput);

                                            return Mono.just(serviceOutput);
                                        });
                            });
                });
    }
}
