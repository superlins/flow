package com.zwtech.flow.domain.model.apidatasource.operation;

import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @author renc
 */
public final class SqlDatasourceOperation implements DatasourceOperation {

    private final String sql;

    public SqlDatasourceOperation(String sql) {
        Assert.hasText(sql, "sql must not be empty");
        this.sql = sql;
    }

    public String sql() {
        return sql;
    }

    @Override
    public boolean sameValueAs(DatasourceOperation other) {
        return other instanceof SqlDatasourceOperation o && Objects.equals(this.sql, o.sql);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DatasourceOperation other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sql);
    }
}