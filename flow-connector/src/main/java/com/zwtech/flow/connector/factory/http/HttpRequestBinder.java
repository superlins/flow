package com.zwtech.flow.connector.factory.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zwtech.flow.connector.binding.MappingEngine;
import com.zwtech.flow.connector.binding.RequestBinder;
import com.zwtech.flow.connector.specs.HttpDatasourceSpecs;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.core.VariableContext;
import com.zwtech.flow.core.parser.TemplateExpressionParser;
import com.zwtech.flow.domain.model.apidatasource.connection.HttpDatasourceConnection;
import com.zwtech.flow.domain.model.apidatasource.operation.HttpDatasourceOperation;
import com.zwtech.flow.domain.shared.MappingSpec;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 请求绑定器
 * <p>
 * 将 ExecutionExchange 转换为 HttpRequestSpec。
 * 支持两种模式：
 * <ul>
 * <li>字符串模板模式（向后兼容）：使用 headersTemplate, queryParamsTemplate 等</li>
 * <li>结构化映射模式（推荐）：使用 MappingSpec + MappingEngine</li>
 * </ul>
 *
 * @author renc
 */
@Component
public final class HttpRequestBinder
        implements RequestBinder<HttpRequestSpec, HttpDatasourceSpecs> {

    private static final TemplateExpressionParser TEMPLATE_PARSER = new TemplateExpressionParser();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final MappingEngine mappingEngine;

    public HttpRequestBinder(MappingEngine mappingEngine) {
        this.mappingEngine = mappingEngine;
    }

    /**
     * 向后兼容的无参构造函数
     */
    public HttpRequestBinder() {
        this.mappingEngine = new MappingEngine();
    }

    @Override
    public HttpRequestSpec bind(ExecutionExchange exchange, HttpDatasourceSpecs specs) {
        HttpDatasourceOperation operation = specs.getOperation();
        HttpDatasourceConnection connection = specs.getConnection();
        VariableContext variableContext = exchange.getVariableContext();

        // 解析 URL（支持模板表达式）
        String url = parseUrl(operation.url(), connection.baseUrl(), variableContext);

        // 解析 Headers、QueryParams、Body（支持两种模式）
        HttpHeaders headers;
        Map<String, Object> queryParams;
        Object body;

        if (operation.useStructuredMappings()) {
            // 新模式：使用 MappingEngine + MappingSpec
            headers = buildHeadersFromMappings(operation.getEffectiveHeadersMappings(), variableContext);
            queryParams = buildMapFromMappings(operation.getEffectiveQueryParamsMappings(), variableContext);
            body = buildBodyFromMappings(operation.getEffectiveBodyMappings(), variableContext);
        } else {
            // 向后兼容：使用字符串模板
            headers = parseJsonHeaders(operation.headersTemplate(), variableContext);
            queryParams = parseJsonMap(operation.queryParamsTemplate(), variableContext);
            body = parseJsonObject(operation.bodyTemplate(), variableContext);
        }

        HttpMethod httpMethod = HttpMethod.valueOf(operation.method());

        return HttpRequestSpec.builder()
                .url(url)
                .method(httpMethod)
                .headers(headers)
                .queryParams(queryParams)
                .body(body)
                .timeout(connection.timeout())
                .retries(connection.maxRetryAttempts() != null ? connection.maxRetryAttempts() : 0)
                .build();
    }

    // ========== 新模式：MappingEngine ==========

    /**
     * 使用 MappingEngine 构建 Headers
     */
    private HttpHeaders buildHeadersFromMappings(MappingSpec spec, VariableContext context) {
        HttpHeaders headers = new HttpHeaders();
        if (spec.isEmpty()) {
            return headers;
        }
        JsonNode result = mappingEngine.applyMappings(spec, context);
        result.fields().forEachRemaining(entry -> headers.add(entry.getKey(), entry.getValue().asText()));
        return headers;
    }

    /**
     * 使用 MappingEngine 构建 Map
     */
    private Map<String, Object> buildMapFromMappings(MappingSpec spec, VariableContext context) {
        if (spec.isEmpty()) {
            return new HashMap<>();
        }
        JsonNode result = mappingEngine.applyMappings(spec, context);
        return OBJECT_MAPPER.convertValue(result, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * 使用 MappingEngine 构建 Body
     */
    private Object buildBodyFromMappings(MappingSpec spec, VariableContext context) {
        if (spec.isEmpty()) {
            return null;
        }
        return mappingEngine.applyMappings(spec, context);
    }

    // ========== 向后兼容模式：字符串模板 ==========

    /**
     * 解析 URL，支持模板表达式和基础 URL 拼接
     */
    private String parseUrl(String urlTemplate, String baseUrl, VariableContext variableContext) {
        if (urlTemplate == null || urlTemplate.isEmpty()) {
            return baseUrl != null ? baseUrl : "";
        }

        // 如果 URL 是相对路径，拼接基础 URL
        String fullUrl = urlTemplate;
        if (baseUrl != null && !baseUrl.isEmpty()) {
            if (urlTemplate.startsWith("/")) {
                String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
                fullUrl = normalizedBase + urlTemplate;
            } else if (!urlTemplate.startsWith("http://") && !urlTemplate.startsWith("https://")) {
                String normalizedBase = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
                fullUrl = normalizedBase + urlTemplate;
            }
        }

        // 解析模板表达式
        Object parsed = TEMPLATE_PARSER.parseTemplate(fullUrl, variableContext);
        return parsed != null ? String.valueOf(parsed) : fullUrl;
    }

    /**
     * 解析 JSON 模板字符串为 Headers（向后兼容）
     */
    @SuppressWarnings("deprecation")
    private HttpHeaders parseJsonHeaders(String template, VariableContext variableContext) {
        HttpHeaders headers = new HttpHeaders();
        if (template == null || template.isEmpty()) {
            return headers;
        }

        String resolved = parseStringTemplate(template, variableContext);
        try {
            Map<String, String> headersMap = OBJECT_MAPPER.readValue(
                    resolved, new TypeReference<Map<String, String>>() {
                    });
            headersMap.forEach(headers::add);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse headers template: " + template, e);
        }
        return headers;
    }

    /**
     * 解析 JSON 模板字符串为 Map（向后兼容）
     */
    @SuppressWarnings("deprecation")
    private Map<String, Object> parseJsonMap(String template, VariableContext variableContext) {
        if (template == null || template.isEmpty()) {
            return new HashMap<>();
        }

        String resolved = parseStringTemplate(template, variableContext);
        try {
            return OBJECT_MAPPER.readValue(resolved, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON map template: " + template, e);
        }
    }

    /**
     * 解析 JSON 模板字符串为 Object（向后兼容）
     */
    @SuppressWarnings("deprecation")
    private Object parseJsonObject(String template, VariableContext variableContext) {
        if (template == null || template.isEmpty()) {
            return null;
        }

        String resolved = parseStringTemplate(template, variableContext);
        try {
            return OBJECT_MAPPER.readValue(resolved, Object.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON object template: " + template, e);
        }
    }

    /**
     * 解析字符串模板
     */
    private String parseStringTemplate(String template, VariableContext variableContext) {
        Object parsed = TEMPLATE_PARSER.parseTemplate(template, variableContext);
        return parsed != null ? String.valueOf(parsed) : template;
    }
}
