package com.zwtech.flow.domain.model.apidatasource.connection;

import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Objects;

/**
 * R2DBC 数据源连接配置
 *
 * @author renc
 */
public final class R2DbcDatasourceConnection implements DatasourceConnection {

    private final String driver;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    // 连接池配置
    private final boolean poolingEnabled;
    private final int maxPoolSize;
    private final int initialPoolSize;
    private final Duration maxLifetime;
    private final Duration maxIdleTime;

    private R2DbcDatasourceConnection(Builder builder) {
        Assert.hasText(builder.driver, "driver must not be empty");
        Assert.hasText(builder.host, "host must not be empty");
        Assert.isTrue(builder.port > 0, "port must be positive");
        Assert.hasText(builder.database, "database must not be empty");
        Assert.hasText(builder.username, "username must not be empty");

        this.driver = builder.driver;
        this.host = builder.host;
        this.port = builder.port;
        this.database = builder.database;
        this.username = builder.username;
        this.password = builder.password;
        this.poolingEnabled = builder.poolingEnabled;
        this.maxPoolSize = builder.maxPoolSize;
        this.initialPoolSize = builder.initialPoolSize;
        this.maxLifetime = builder.maxLifetime;
        this.maxIdleTime = builder.maxIdleTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String driver() {
        return driver;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String database() {
        return database;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public boolean isPoolingEnabled() {
        return poolingEnabled;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getInitialPoolSize() {
        return initialPoolSize;
    }

    public Duration getMaxLifetime() {
        return maxLifetime;
    }

    public Duration getMaxIdleTime() {
        return maxIdleTime;
    }

    @Override
    public boolean sameValueAs(DatasourceConnection other) {
        if (!(other instanceof R2DbcDatasourceConnection o)) return false;
        return Objects.equals(driver, o.driver)
                && Objects.equals(host, o.host)
                && port == o.port
                && Objects.equals(database, o.database)
                && Objects.equals(username, o.username);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DatasourceConnection other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driver, host, port, database, username);
    }

    @Override
    public String toString() {
        return "R2DbcDatasourceConnection{" +
                "driver='" + driver + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", database='" + database + '\'' +
                ", username='" + username + '\'' +
                ", poolingEnabled=" + poolingEnabled +
                '}';
    }

    /**
     * Builder for R2DbcDatasourceConnection
     */
    public static class Builder {
        private String driver;
        private String host;
        private int port;
        private String database;
        private String username;
        private String password;

        private boolean poolingEnabled = true;
        private int maxPoolSize = 10;
        private int initialPoolSize = 2;
        private Duration maxLifetime = Duration.ofMinutes(30);
        private Duration maxIdleTime = Duration.ofMinutes(10);

        public Builder driver(String driver) {
            this.driver = driver;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder poolingEnabled(boolean poolingEnabled) {
            this.poolingEnabled = poolingEnabled;
            return this;
        }

        public Builder maxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
            return this;
        }

        public Builder initialPoolSize(int initialPoolSize) {
            this.initialPoolSize = initialPoolSize;
            return this;
        }

        public Builder maxLifetime(Duration maxLifetime) {
            this.maxLifetime = maxLifetime;
            return this;
        }

        public Builder maxIdleTime(Duration maxIdleTime) {
            this.maxIdleTime = maxIdleTime;
            return this;
        }

        public R2DbcDatasourceConnection build() {
            return new R2DbcDatasourceConnection(this);
        }
    }
}