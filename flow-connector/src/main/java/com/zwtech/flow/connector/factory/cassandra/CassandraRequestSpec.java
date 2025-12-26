package com.zwtech.flow.connector.factory.cassandra;

import org.example.core.connector.RequestSpec;

public class CassandraRequestSpec implements RequestSpec {
    private String cqlQuery;
    private Object[] bindValues;
    // ... 其他查询参数，如一致性级别、分页信息等


    public CassandraRequestSpec() {
    }

    public CassandraRequestSpec(String cqlQuery, Object... bindValues) {
        this.cqlQuery = cqlQuery;
        this.bindValues = bindValues;
    }

    public String getCqlQuery() {
        return cqlQuery;
    }

    public Object[] getBindValues() {
        return bindValues;
    }

    public void validate() {
        if (cqlQuery == null || cqlQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("CQL query cannot be null or empty.");
        }
    }
}