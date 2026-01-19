package com.zwtech.flow.domain.model.apidatasource.operation;

import org.springframework.util.Assert;

/**
 * HTTP 操作规范
 * <p>
 * 存储前端配置的原始数据，所有字段都是模板字符串。
 * 表达式由执行层的解析引擎处理，领域模型不关心技术细节。
 *
 * @author renc
 */
public final class HttpDatasourceOperation implements DatasourceOperation {

    private final String url;
    private final String method;
    private final String headersTemplate;
    private final String queryParamsTemplate;
    private final String bodyTemplate;
    private final String responseBodyTemplate;

    public HttpDatasourceOperation(
            String url,
            String method,
            String headersTemplate,
            String queryParamsTemplate,
            String bodyTemplate,
            String responseBodyTemplate) {
        Assert.hasText(url, "url must not be empty");
        Assert.hasText(method, "method must not be empty");
        this.url = url;
        this.method = method;
        this.headersTemplate = headersTemplate;
        this.queryParamsTemplate = queryParamsTemplate;
        this.bodyTemplate = bodyTemplate;
        this.responseBodyTemplate = responseBodyTemplate;
    }

    /**
     * URL 模板字符串
     * 可能包含表达式，如 "https://api.com/user/{{ #dsInput.userId }}/orders"
     */
    public String url() {
        return url;
    }

    /**
     * HTTP 方法（静态值）：GET/POST/PUT/DELETE
     */
    public String method() {
        return method;
    }

    /**
     * Headers 模板字符串（JSON 格式）
     * 例如："{\"Authorization\":\"Bearer {{ #dsInput.token }}\"}"
     */
    public String headersTemplate() {
        return headersTemplate;
    }

    /**
     * Query Params 模板字符串（JSON 格式）
     * 例如："{\"userId\":\"{{ #dsInput.userId }}\"}"
     */
    public String queryParamsTemplate() {
        return queryParamsTemplate;
    }

    /**
     * Request Body 模板字符串（JSON 格式）
     * 例如："{\"user\":{\"id\":\"{{ #dsInput.userId }}\"}}"
     */
    public String bodyTemplate() {
        return bodyTemplate;
    }

    /**
     * Response Body 提取规则字符串（JSON 格式，SpEL 表达式）
     * 例如："{\"orderId\":\"{{ #resp.body.orderId }}\",\"status\":\"{{ #resp.status }}\"}"
     * 如果为 null 或空，表示返回整个响应体
     */
    public String responseBodyTemplate() {
        return responseBodyTemplate;
    }

    @Override
    public boolean sameValueAs(DatasourceOperation other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HttpDatasourceOperation that)) {
            return false;
        }
        return this.url.equals(that.url)
                && this.method.equals(that.method)
                && equals(this.headersTemplate, that.headersTemplate)
                && equals(this.queryParamsTemplate, that.queryParamsTemplate)
                && equals(this.bodyTemplate, that.bodyTemplate)
                && equals(this.responseBodyTemplate, that.responseBodyTemplate);
    }

    private boolean equals(String a, String b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DatasourceOperation other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + method.hashCode();
        return result;
    }
}
