package com.zwtech.flow.domain.model.apidatasource.operation;

import org.springframework.util.Assert;

/**
 * SQL 操作规范
 * <p>
 * 存储前端配置的 SQL 语句和参数绑定规则。
 * 所有字段都是模板字符串，表达式由执行层的解析引擎处理。
 *
 * @author renc
 */
public final class SqlDatasourceOperation implements DatasourceOperation {

    private final String sql;
    private final String paramsTemplate;
    private final String responseBodyTemplate;

    public SqlDatasourceOperation(String sql, String paramsTemplate, String responseBodyTemplate) {
        Assert.hasText(sql, "sql must not be empty");
        this.sql = sql;
        this.paramsTemplate = paramsTemplate;
        this.responseBodyTemplate = responseBodyTemplate;
    }

    /**
     * SQL 语句（可能包含参数占位符）
     * 例如："SELECT * FROM orders WHERE user_id = :userId"
     */
    public String sql() {
        return sql;
    }

    /**
     * SQL 参数绑定规则（JSON 格式模板字符串）
     *例如："{\"userId\":\"{{ #dsInput.userId }}\",\"limit\":\"{{ #dsInput.limit }}\"}"
     * 解析后传递给 DatabaseClient 作为参数绑定
     */
    public String paramsTemplate() {
        return paramsTemplate;
    }

    /**
     * Response Body 提取规则字符串（JSON 格式，SpEL 表达式）
     * 例如："{\"orderId\":\"{{ #rows[0].orderId }}\",\"status\":\"{{ #rows[0].status }}\"}"
     * 对于单行查询，可以直接提取字段
     * 对于多行查询，可以提取整个数组
     * 如果为 null 或空，表示返回整个查询结果（rows 数组）
     */
    public String responseBodyTemplate() {
        return responseBodyTemplate;
    }

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
        int result = sql.hashCode();
        result = 31 * result + (paramsTemplate != null ? paramsTemplate.hashCode() : 0);
        return result;
    }
}
