package com.zwtech.flow.domain.model.apidatasource.operation;

import com.zwtech.flow.domain.shared.MappingSpec;
import org.springframework.util.Assert;

/**
 * SQL 操作规范
 * <p>
 * 存储前端配置的 SQL 语句和参数绑定规则。
 * 支持两种格式：
 * <ul>
 * <li>字符串模板格式（向后兼容）：paramsTemplate, responseBodyTemplate</li>
 * <li>结构化映射格式（推荐）：paramsMappings, responseMappings</li>
 * </ul>
 * <p>
 * 表达式由执行层的 MappingEngine 处理。
 *
 * @author renc
 */
public final class SqlDatasourceOperation implements DatasourceOperation {

    private final String sql;

    // 向后兼容：字符串模板格式
    private final String paramsTemplate;
    private final String responseBodyTemplate;

    // 新增：结构化映射格式（可选）
    private final MappingSpec paramsMappings;
    private final MappingSpec responseMappings;

    /**
     * 向后兼容构造函数（使用字符串模板）
     */
    public SqlDatasourceOperation(String sql, String paramsTemplate, String responseBodyTemplate) {
        this(sql, paramsTemplate, responseBodyTemplate, null, null);
    }

    /**
     * 完整构造函数（支持两种格式混合使用）
     */
    public SqlDatasourceOperation(
            String sql,
            String paramsTemplate,
            String responseBodyTemplate,
            MappingSpec paramsMappings,
            MappingSpec responseMappings) {
        Assert.hasText(sql, "sql must not be empty");
        this.sql = sql;
        this.paramsTemplate = paramsTemplate;
        this.responseBodyTemplate = responseBodyTemplate;
        this.paramsMappings = paramsMappings;
        this.responseMappings = responseMappings;
    }

    /**
     * 使用结构化映射创建（推荐）
     */
    public static SqlDatasourceOperation withMappings(
            String sql,
            MappingSpec paramsMappings,
            MappingSpec responseMappings) {
        return new SqlDatasourceOperation(sql, null, null, paramsMappings, responseMappings);
    }

    // ========== SQL ==========

    /**
     * SQL 语句（可能包含参数占位符）
     * 例如："SELECT * FROM orders WHERE user_id = :userId"
     */
    public String sql() {
        return sql;
    }

    // ========== 向后兼容：字符串模板 ==========

    /**
     * SQL 参数绑定规则（JSON 格式模板字符串）
     * 例如："{\"userId\":\"{{ #dsInput.userId }}\"}"
     *
     * @deprecated 推荐使用 {@link #paramsMappings()}
     */
    @Deprecated(since = "0.0.1")
    public String paramsTemplate() {
        return paramsTemplate;
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
     * SQL 参数结构化映射
     *
     * @return MappingSpec，如果未设置则返回 null
     */
    public MappingSpec paramsMappings() {
        return paramsMappings;
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
        return paramsMappings != null || responseMappings != null;
    }

    /**
     * 获取有效的 Params 映射
     */
    public MappingSpec getEffectiveParamsMappings() {
        return paramsMappings != null ? paramsMappings : MappingSpec.empty();
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
        if (!(other instanceof SqlDatasourceOperation that)) {
            return false;
        }
        return this.sql.equals(that.sql)
                && equals(this.paramsTemplate, that.paramsTemplate)
                && equals(this.responseBodyTemplate, that.responseBodyTemplate)
                && equals(this.paramsMappings, that.paramsMappings)
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
        int result = sql.hashCode();
        result = 31 * result + (paramsTemplate != null ? paramsTemplate.hashCode() : 0);
        return result;
    }
}
