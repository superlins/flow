package com.zwtech.flow.connector.factory.r2dbc;

import org.example.core.connector.Connector;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

public class R2dbcConnector implements Connector<R2dbcRequestSpec, R2dbcResponseSpec> {

    private final DatabaseClient databaseClient;

    public R2dbcConnector(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<R2dbcResponseSpec> connect(R2dbcRequestSpec spec) {
        DatabaseClient.GenericExecuteSpec exec = databaseClient.sql(spec.getSql());
        for (int i = 0; i < spec.getParameters().size(); i++) {
            exec = exec.bind(i, spec.getParameters().get(i));
        }

        return exec.fetch().all()
            .collectList()
            .map(rows -> new R2dbcResponseSpec().rows(rows));
    }
}