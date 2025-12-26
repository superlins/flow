package com.zwtech.flow.connector.factory.cassandra;

public class CassandraConnectionEndpoint {
    private String contactPoints;
    private int port;
    private String keyspace;
    private String localDatacenter; // 增加 localDatacenter
    // ... 其他连接配置，如用户名、密码、SSL等

    public CassandraConnectionEndpoint(String contactPoints, int port, String keyspace, String localDatacenter) {
        this.contactPoints = contactPoints;
        this.port = port;
        this.keyspace = keyspace;
        this.localDatacenter = localDatacenter;
    }

    public String getContactPoints() {
        return contactPoints;
    }

    public int getPort() {
        return port;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public String getLocalDatacenter() {
        return localDatacenter;
    }

    // 重写 equals 和 hashCode 方法，以便在 Map 中作为 Key 使用
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CassandraConnectionEndpoint that = (CassandraConnectionEndpoint) o;
        return port == that.port && contactPoints.equals(that.contactPoints) && keyspace.equals(that.keyspace) && java.util.Objects.equals(localDatacenter, that.localDatacenter);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(contactPoints, port, keyspace, localDatacenter);
    }
}