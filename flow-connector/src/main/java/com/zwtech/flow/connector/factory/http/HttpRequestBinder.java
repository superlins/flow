package com.zwtech.flow.connector.factory.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zwtech.flow.connector.RequestSpec;
import com.zwtech.flow.connector.binding.RequestBinder;
import com.zwtech.flow.connector.specs.HttpDatasourceSpecs;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.core.VariableContext;
import com.zwtech.flow.core.parser.TemplateExpressionParser;
import com.zwtech.flow.domain.model.apidatasource.connection.HttpDatasourceConnection;
import com.zwtech.flow.domain.model.apidatasource.operation.HttpDatasourceOperation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 请求绑定器
 * <p>
 * 将 ExecutionExchange 转换为 HttpRequestSpec。
 * 使用 VariableContext 解析模板表达式。
 *
 * @author renc
 */
public final class HttpRequestBinder
        implements RequestBinder<HttpRequestSpec, HttpDatasourceSpecs> {

    private static final TemplateExpressionParser TEMPLATE_PARSER = new TemplateExpressionParser();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public HttpRequestBinder() {
    }

    @Override
    public HttpRequestSpec bind(ExecutionExchange exchange, HttpDatasourceSpecs specs) {
        HttpDatasourceOperation operation = specs.getOperation();
        HttpDatasourceConnection connection = specs.getConnection();

        // 使用统一的 VariableContext
        VariableContext variableContext = exchange.getVariableContext();

        // 解析 URL（支持模板表达式）
        String url = parseUrl(operation.url(), connection.baseUrl(), variableContext);

        // 解析 Headers（JSON 模板字符串）
        HttpHeaders headers = parseJsonHeaders(operation.headersTemplate(), variableContext);

        // 解析 QueryParams（JSON 模板字符串）
        Map<String, Object> queryParams = parseJsonMap(operation.queryParamsTemplate(), variableContext);

        // 解析 Body（JSON 模板字符串）
        Object body = parseJsonObject(operation.bodyTemplate(), variableContext);

        HttpMethod httpMethod = HttpMethod.valueOf(operation.method());

        // 使用连接配置中的 timeout，如果没有则使用默认值
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
     * 解析 JSON 模板字符串为 Headers
     * 例如："{\"Authorization\":\"Bearer {{ #dsInput.token }}\"}"
     */
    private HttpHeaders parseJsonHeaders(String template, VariableContext variableContext) {
        HttpHeaders headers = new HttpHeaders();
        if (template == null || template.isEmpty()) {
            return headers;
        }

        String resolved = parseStringTemplate(template, variableContext);
        try {
            Map<String, String> headersMap = OBJECT_MAPPER.readValue(
                    resolved, new TypeReference<Map<String, String>>() {});
            headersMap.forEach((k, v) -> headers.add(k, v));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse headers template: " + template, e);
        }
        return headers;
    }

    /**
     * 解析 JSON 模板字符串为 Map
     * 例如："{\"userId\":\"{{ #dsInput.userId }}\",\"limit\":\"{{ #dsInput.limit }}\"}"
     */
    private Map<String, Object> parseJsonMap(String template, VariableContext variableContext) {
        if (template == null || template.isEmpty()) {
            return new HashMap<>();
        }

        String resolved = parseStringTemplate(template, variableContext);
        try {
            return OBJECT_MAPPER.readValue(resolved, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON map template: " + template, e);
        }
    }

    /**
     * 解析 JSON 模板字符串为 Object
     * 例如："{\"user\":{\"id\":\"{{ #dsInput.userId }}\"}}"
     */
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
