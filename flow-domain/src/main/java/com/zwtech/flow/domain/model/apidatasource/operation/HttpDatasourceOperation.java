package com.zwtech.flow.domain.model.apidatasource.operation;

import com.zwtech.flow.domain.shared.MappingSpec;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * HTTP 操作规范
 * <p>
 * 存储前端配置的原始数据，支持两种格式：
 * <ul>
 * <li>字符串模板格式（向后兼容）：headersTemplate, queryParamsTemplate, bodyTemplate,
 * responseBodyTemplate</li>
 * <li>结构化映射格式（推荐）：headersMappings, queryParamsMappings, bodyMappings,
 * responseMappings</li>
 * </ul>
 * <p>
 * 表达式由执行层的解析引擎（MappingEngine）处理，领域模型不关心技术细节。
 *
 * @author renc
 */
public final class HttpDatasourceOperation implements DatasourceOperation {

    private final String url;
    private final String method;

    // 向后兼容：字符串模板格式
    private final String headersTemplate;
    private final String queryParamsTemplate;
    private final String bodyTemplate;
    private final String responseBodyTemplate;

    // 新增：结构化映射格式（可选）
    private final MappingSpec headersMappings;
    private final MappingSpec queryParamsMappings;
    private final MappingSpec bodyMappings;
    private final MappingSpec responseMappings;

    /**
     * 向后兼容构造函数（使用字符串模板）
     */
    public HttpDatasourceOperation(
            String url,
            String method,
            String headersTemplate,
            String queryParamsTemplate,
            String bodyTemplate,
            String responseBodyTemplate) {
        this(url, method, headersTemplate, queryParamsTemplate, bodyTemplate, responseBodyTemplate,
                null, null, null, null);
    }

    /**
     * 完整构造函数（支持两种格式混合使用）
     */
    public HttpDatasourceOperation(
            String url,
            String method,
            String headersTemplate,
            String queryParamsTemplate,
            String bodyTemplate,
            String responseBodyTemplate,
            MappingSpec headersMappings,
            MappingSpec queryParamsMappings,
            MappingSpec bodyMappings,
            MappingSpec responseMappings) {
        Assert.hasText(url, "url must not be empty");
        Assert.hasText(method, "method must not be empty");
        this.url = url;
        this.method = method;
        this.headersTemplate = headersTemplate;
        this.queryParamsTemplate = queryParamsTemplate;
        this.bodyTemplate = bodyTemplate;
        this.responseBodyTemplate = responseBodyTemplate;
        this.headersMappings = headersMappings;
        this.queryParamsMappings = queryParamsMappings;
        this.bodyMappings = bodyMappings;
        this.responseMappings = responseMappings;
    }

    /**
     * 使用结构化映射创建（推荐）
     */
    public static HttpDatasourceOperation withMappings(
            String url,
            String method,
            MappingSpec headersMappings,
            MappingSpec queryParamsMappings,
            MappingSpec bodyMappings,
            MappingSpec responseMappings) {
        return new HttpDatasourceOperation(
                url, method, null, null, null, null,
                headersMappings, queryParamsMappings, bodyMappings, responseMappings);
    }

    // ========== URL 和 Method ==========

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

    // ========== 向后兼容：字符串模板 ==========

    /**
     * Headers 模板字符串（JSON 格式）
     * 例如："{\"Authorization\":\"Bearer {{ #dsInput.token }}\"}"
     *
     * @deprecated 推荐使用 {@link #headersMappings()}
     */
    @Deprecated(since = "0.0.1")
    public String headersTemplate() {
        return headersTemplate;
    }

    /**
     * Query Params 模板字符串（JSON 格式）
     *
     * @deprecated 推荐使用 {@link #queryParamsMappings()}
     */
    @Deprecated(since = "0.0.1")
    public String queryParamsTemplate() {
        return queryParamsTemplate;
    }

    /**
     * Request Body 模板字符串（JSON 格式）
     *
     * @deprecated 推荐使用 {@link #bodyMappings()}
     */
    @Deprecated(since = "0.0.1")
    public String bodyTemplate() {
        return bodyTemplate;
    }

    /**
     * Response Body 提取规则字符串（JSON 格式，SpEL 表达式）
     *
     * @deprecated 推荐使用 {@link #responseMappings()}
     */
    @Deprecated(since = "0.0.1")
    public String responseBodyTemplate() {
        return responseBodyTemplate;
    }

    // ========== 结构化映射（推荐） ==========

    /**
     * Headers 结构化映射
     *
     * @return MappingSpec，如果未设置则返回 null
     */
    public MappingSpec headersMappings() {
        return headersMappings;
    }

    /**
     * Query Params 结构化映射
     *
     * @return MappingSpec，如果未设置则返回 null
     */
    public MappingSpec queryParamsMappings() {
        return queryParamsMappings;
    }

    /**
     * Body 结构化映射
     *
     * @return MappingSpec，如果未设置则返回 null
     */
    public MappingSpec bodyMappings() {
        return bodyMappings;
    }

    /**
     * Response 结构化映射
     *
     * @return MappingSpec，如果未设置则返回 null
     */
    public MappingSpec responseMappings() {
        return responseMappings;
    }

    // ========== 辅助方法 ==========

    /**
     * 是否使用结构化映射模式
     */
    public boolean useStructuredMappings() {
        return headersMappings != null || queryParamsMappings != null
                || bodyMappings != null || responseMappings != null;
    }

    /**
     * 获取有效的 Headers 映射
     * 优先返回结构化映射，如果未设置则返回空映射
     */
    public MappingSpec getEffectiveHeadersMappings() {
        return headersMappings != null ? headersMappings : MappingSpec.empty();
    }

    /**
     * 获取有效的 Query Params 映射
     */
    public MappingSpec getEffectiveQueryParamsMappings() {
        return queryParamsMappings != null ? queryParamsMappings : MappingSpec.empty();
    }

    /**
     * 获取有效的 Body 映射
     */
    public MappingSpec getEffectiveBodyMappings() {
        return bodyMappings != null ? bodyMappings : MappingSpec.empty();
    }

    /**
     * 获取有效的 Response 映射
     */
    public MappingSpec getEffectiveResponseMappings() {
        return responseMappings != null ? responseMappings : MappingSpec.empty();
    }

    // ========== ValueObject ==========

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
                && equals(this.responseBodyTemplate, that.responseBodyTemplate)
                && equals(this.headersMappings, that.headersMappings)
                && equals(this.queryParamsMappings, that.queryParamsMappings)
                && equals(this.bodyMappings, that.bodyMappings)
                && equals(this.responseMappings, that.responseMappings);
    }

    private boolean equals(Object a, Object b) {
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
