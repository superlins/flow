package com.zwtech.flow.connector.factory.http;

import com.zwtech.flow.connector.RequestSpec;
import com.zwtech.flow.connector.binding.RequestBinder;
import com.zwtech.flow.connector.specs.DatasourceSpecs;
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
                .retries(0)
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
     * 解析 Headers，支持模板表达式
     */
    private HttpHeaders parseHeaders(Map<String, String> headersTemplate, VariableContext variableContext) {
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
    private Map<String, Object> parseQueryParams(Map<String, Object> queryParamsTemplate, VariableContext variableContext) {
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
    private Object parseBody(Map<String, Object> bodyTemplate, VariableContext variableContext) {
        if (bodyTemplate == null || bodyTemplate.isEmpty()) {
            return null;
        }
        return TEMPLATE_PARSER.parseObject(bodyTemplate, variableContext);
    }
}
