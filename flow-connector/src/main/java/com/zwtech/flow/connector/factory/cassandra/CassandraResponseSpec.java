package com.zwtech.flow.connector.factory.cassandra;

import org.example.core.connector.ResponseSpec;

import java.util.Map;

public class CassandraResponseSpec implements ResponseSpec {

    private Map<String, Object> resultSet;
    // ... 其他响应数据，如错误信息等

    public CassandraResponseSpec(Map<String, Object> resultSet) {
        this.resultSet = resultSet;
    }

    public Map<String, Object> getResultSet() {
        return resultSet;
    }

    // Helper methods to extract data from resultSet
}