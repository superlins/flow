package com.zwtech.flow.connector.factory.r2dbc;

import com.zwtech.flow.connector.Connector;
import com.zwtech.flow.connector.ExecutionAttributes;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class R2dbcConnector implements Connector<R2dbcRequestSpec, R2dbcResponseSpec> {

    private final DatabaseClient databaseClient;

    public R2dbcConnector(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<R2dbcResponseSpec> connect(R2dbcRequestSpec spec, ExecutionAttributes attributes) {
        var exec = databaseClient.sql(spec.getSql());
        for (int i = 0; i < spec.getParameters().size(); i++) {
            exec = exec.bind(i, spec.getParameters().get(i));
        }

        return exec.fetch()
                .all()
                .collectList()
                .map(rows -> {
                    var jsonRows = new ObjectMapper().convertValue(rows, new TypeReference<List<JsonNode>>() {
                    });
                    return R2dbcResponseSpec.builder().rows(jsonRows).build();
                });
    }
}