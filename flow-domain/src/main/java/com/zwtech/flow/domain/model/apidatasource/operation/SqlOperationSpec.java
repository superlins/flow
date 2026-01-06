package com.zwtech.flow.domain.model.apidatasource.operation;

import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @author renc
 */
public final class SqlOperationSpec implements OperationSpec {

    private final String sql;

    public SqlOperationSpec(String sql) {
        Assert.hasText(sql, "sql must not be empty");
        this.sql = sql;
    }

    public String sql() {
        return sql;
    }

    @Override
    public boolean sameValueAs(OperationSpec other) {
        return other instanceof SqlOperationSpec o && Objects.equals(this.sql, o.sql);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof OperationSpec other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sql);
    }
}