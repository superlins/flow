package com.zwtech.flow.connector.factory.http;

import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.operation.HttpOperationSpec;
import tools.jackson.databind.JsonNode;

/**
 * 将下游 HttpResponseSpec + HttpOperationSpec 中的响应映射配置
 * 绑定为 Datasource 级别的输出 JSON（用于写入 ExecutionExchange.response）。
 * <p>
 * 当前实现直接返回响应体 JsonNode，后续可基于 operation.responseBody 模板
 * 和 $response.body / $response.headers 实现更灵活的映射。
 */
public final class HttpResponseBinder {

    private HttpResponseBinder() {
    }

    public static JsonNode bind(HttpResponseSpec responseSpec, ApiDatasource datasource) {
        HttpOperationSpec operation = (HttpOperationSpec) datasource.operation();
        // TODO: 使用 operation.responseBody 模板 + $response.body/$response.headers
        // 生成一个满足 DatasourceContract.output 的 JSON
        return responseSpec.getBody();
    }
}
