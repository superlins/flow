package com.zwtech.flow.domain.model.apidatasource.r2dbc;

import com.zwtech.flow.domain.model.apidatasource.*;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

/**
 * @author renc
 */
public class R2dbcApiDatasourceRepository implements ApiDatasourceRepository {

    private final DatabaseClient client;

    public R2dbcApiDatasourceRepository(DatabaseClient client) {
        this.client = client;
    }

    @Override
    public Mono<ApiDatasource> find(DatasourceId id) {
        return client.sql("SELECT * FROM api_datasource WHERE ds_key = :key AND ds_version = :version")
                .bind("key", id.key().value())
                .bind("version", id.version().value())
                .map(row -> {
                    var inputSchema = row.get("input_schema", String.class);
                    var outputSchema = row.get("output_schema", String.class);
                    var strict = row.get("strict", Boolean.class);
                    var contract = new DatasourceContract(inputSchema, outputSchema, Boolean.TRUE.equals(strict));
                    return ApiDatasource.create(id, DatasourceType.valueOf(row.get("ds_type", String.class)), contract);
                })
                .one();
    }

    @Override
    public Mono<Void> save(ApiDatasource ds) {
        return client.sql("""
                        INSERT INTO api_datasource (ds_key, ds_version, ds_type, status, input_schema, output_schema, strict)
                        VALUES (:key, :version, :type, :status, :in, :out, :strict)
                        """)
                .bind("key", ds.id().key().value())
                .bind("version", ds.id().version().value())
                .bind("type", ds.type().name())
                .bind("status", ds.status().name())
                .bind("in", ds.contract().inputSchema())
                .bind("out", ds.contract().outputSchema())
                .bind("strict", ds.contract().strict())
                .then();
    }
}
