package com.zwtech.flow.connector.factory.http;

import com.zwtech.flow.core.DefaultVariableContext;
import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.core.VariableContext;
import com.zwtech.flow.core.parser.TemplateExpressionParser;
import com.zwtech.flow.domain.model.apidatasource.behavior.HttpOperationBehavior;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * 将 Datasource 级别的 ExecutionExchange.request（JSON）
 * 与 HttpOperationBehavior（静态 HTTP 模板）绑定为一次具体调用所需的 HttpRequestSpec。
 * <p>
 * 支持解析模板表达式（例如 {{ $request.userId }}）
 */
public final class HttpRequestBinder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TemplateExpressionParser TEMPLATE_PARSER = new TemplateExpressionParser();

    private HttpRequestBinder() {
    }

    /**
     * 绑定 ExecutionExchange 和 HttpOperationBehavior 为 HttpRequestSpec
     * 
     * @param exchange ExecutionExchange
     * @param operation HttpOperationBehavior
     * @param connectionBaseUrl 连接的基础 URL（从 ConnectionSpec 获取）
     * @return HttpRequestSpec
     */
    public static HttpRequestSpec bind(ExecutionExchange exchange, HttpOperationBehavior operation, String connectionBaseUrl) {
        // 创建变量上下文
        VariableContext variableContext = new DefaultVariableContext(exchange.getRequest(), exchange.getResponse());

        // 解析 URL（支持模板表达式）
        String url = parseUrl(operation.url(), connectionBaseUrl, variableContext);

        // 解析 Headers（支持模板表达式）
        HttpHeaders headers = parseHeaders(operation.headers(), variableContext);

        // 解析 QueryParams（支持模板表达式）
        Map<String, Object> queryParams = parseQueryParams(operation.queryParams(), variableContext);

        // 解析 Body（支持模板表达式）
        Object body = parseBody(operation.requestBody(), variableContext);

        HttpMethod httpMethod = HttpMethod.valueOf(operation.method());

        return HttpRequestSpec.builder()
                .url(url)
                .method(httpMethod)
                .headers(headers)
                .queryParams(queryParams)
                .body(body)
                .timeout(operation.timeout())
                .retries(0) // 默认不重试，可通过配置设置
                .build();
    }

    /**
     * 解析 URL，支持模板表达式和基础 URL 拼接
     */
    private static String parseUrl(String urlTemplate, String baseUrl, VariableContext variableContext) {
        if (urlTemplate == null || urlTemplate.isEmpty()) {
            return baseUrl != null ? baseUrl : "";
        }

        // 如果 URL 是相对路径，拼接基础 URL
        String fullUrl = urlTemplate;
        if (baseUrl != null && !baseUrl.isEmpty()) {
            if (urlTemplate.startsWith("/")) {
                // 移除 baseUrl 末尾的斜杠
                String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
                fullUrl = normalizedBase + urlTemplate;
            } else if (!urlTemplate.startsWith("http://") && !urlTemplate.startsWith("https://")) {
                // 相对路径，拼接基础 URL
                String normalizedBase = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
                fullUrl = normalizedBase + urlTemplate;
            }
        }

        // 解析模板表达式
        Object parsed = TEMPLATE_PARSER.parseTemplate(fullUrl, variableContext);
        return parsed != null ? String.valueOf(parsed) : fullUrl;
    }

    /**
     * 解析 Headers，支持模板表达式
     */
    private static HttpHeaders parseHeaders(Map<String, String> headersTemplate, VariableContext variableContext) {
        HttpHeaders headers = new HttpHeaders();
        if (headersTemplate != null) {
            headersTemplate.forEach((k, v) -> {
                if (v != null) {
                    Object parsed = TEMPLATE_PARSER.parseTemplate(v, variableContext);
                    headers.add(k, parsed != null ? String.valueOf(parsed) : v);
                }
            });
        }
        return headers;
    }

    /**
     * 解析 QueryParams，支持模板表达式
     */
    private static Map<String, Object> parseQueryParams(Map<String, Object> queryParamsTemplate, VariableContext variableContext) {
        Map<String, Object> queryParams = new HashMap<>();
        if (queryParamsTemplate != null) {
            queryParamsTemplate.forEach((k, v) -> {
                Object parsed = TEMPLATE_PARSER.parseObject(v, variableContext);
                queryParams.put(k, parsed);
            });
        }
        return queryParams;
    }

    /**
     * 解析 Body，支持模板表达式
     */
    private static Object parseBody(Map<String, Object> bodyTemplate, VariableContext variableContext) {
        if (bodyTemplate == null || bodyTemplate.isEmpty()) {
            return null;
        }
        return TEMPLATE_PARSER.parseObject(bodyTemplate, variableContext);
    }
}
