package com.zwtech.flow.domain.model.apidatasource.behavior;

import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @author renc
 */
public final class SqlOperationBehavior implements OperationBehavior {

    private final String sql;

    public SqlOperationBehavior(String sql) {
        Assert.hasText(sql, "sql must not be empty");
        this.sql = sql;
    }

    public String sql() {
        return sql;
    }

    @Override
    public boolean sameValueAs(OperationBehavior other) {
        return other instanceof SqlOperationBehavior o && Objects.equals(this.sql, o.sql);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof OperationBehavior other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sql);
    }
}