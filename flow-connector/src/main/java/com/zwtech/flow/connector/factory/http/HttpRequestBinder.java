package com.zwtech.flow.connector.factory.http;

import com.zwtech.flow.core.ExecutionExchange;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.behavior.HttpOperationBehavior;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * 将 Datasource 级别的 ExecutionExchange.request（JSON）
 * 与 HttpOperationSpec（静态 HTTP 模板）绑定为一次具体调用所需的 HttpRequestSpec。
 * <p>
 * 当前实现仅做直传绑定，不解析模板表达式（例如 {{ $request.userId }}），
 * 后续可在此处集成表达式引擎。
 */
public final class HttpRequestBinder {

    private HttpRequestBinder() {
    }

    public static HttpRequestSpec bind(ExecutionExchange exchange, ApiDatasource datasource) {
        HttpOperationBehavior operation = (HttpOperationBehavior) datasource.operation();

        // Datasource input JSON：未来可作为模板环境中的 $request 使用
        JsonNode requestNode = exchange.getRequest();
        // TODO: 使用 requestNode + operation 中的模板字段渲染 URL、query、headers、body

        HttpHeaders headers = new HttpHeaders();
        if (operation.headers() != null) {
            operation.headers().forEach((k, v) -> {
                if (v != null) {
                    headers.add(k, String.valueOf(v));
                }
            });
        }

        Map<String, Object> queryParams = new HashMap<>();
        if (operation.queryParams() != null) {
            queryParams.putAll(operation.queryParams());
        }

        Object body = operation.requestBody();

        HttpMethod httpMethod = HttpMethod.valueOf(operation.method());

        return HttpRequestSpec.builder()
                .url(operation.url())
                .method(httpMethod)
                .headers(headers)
                .queryParams(queryParams)
                .body(body)
                .timeout(operation.timeout())
                .retries(operation.retries())
                .build();
    }
}
