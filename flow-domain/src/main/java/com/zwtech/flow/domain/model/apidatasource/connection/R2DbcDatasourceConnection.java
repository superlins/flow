package com.zwtech.flow.domain.model.apidatasource.connection;

import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @author renc
 */
public final class R2DbcDatasourceConnection implements DatasourceConnection {

    private final String host;
    private final int port;
    private final String database;

    public R2DbcDatasourceConnection(String host, int port, String database) {
        Assert.hasText(host, "host must not be empty");
        Assert.isTrue(port > 0, "port must be positive");
        Assert.hasText(database, "database must not be empty");
        this.host = host;
        this.port = port;
        this.database = database;
    }

    @Override
    public boolean sameValueAs(DatasourceConnection other) {
        if (!(other instanceof R2DbcDatasourceConnection o)) return false;
        return Objects.equals(host, o.host)
                && port == o.port
                && Objects.equals(database, o.database);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DatasourceConnection other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, database);
    }
}