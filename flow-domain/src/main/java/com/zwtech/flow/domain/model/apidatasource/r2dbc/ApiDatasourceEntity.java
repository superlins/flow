package com.zwtech.flow.domain.model.apidatasource.r2dbc;

import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import com.zwtech.flow.domain.model.apidatasource.DatasourceId;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("FLW_API_DATASOURCE")
class ApiDatasourceEntity {

    @Id
    @Column("ID_")
    private Long id;

    @Column("KEY_")
    private String key;

    @Column("VERSION_")
    private Integer version;

    @Column("TYPE_")
    private String type;

    @Column("STATUS_")
    private String status;

    @Column("NAME_")
    private String name;

    @Column("DESCRIPTION_")
    private String description;

    @Column("INPUT_SCHEMA_")
    private String inputSchema;

    @Column("OUTPUT_SCHEMA_")
    private String outputSchema;

    @Column("OPERATION_")
    private String operation;

    @Column("CONNECTION_")
    private String connection;

    @Column("EXTENSION_")
    private String extension;

    @Column("CREATED_AT_")
    private Instant createdAt;

    @Column("UPDATED_AT_")
    private Instant updatedAt;

    public static ApiDatasourceEntity fromApiDatasource(ApiDatasource ds) {
        var apiDatasourceEntity = new ApiDatasourceEntity();
        apiDatasourceEntity.setKey(ds.id().key());
        apiDatasourceEntity.setVersion(ds.id().version());
        return apiDatasourceEntity;
    }

    public ApiDatasource toApiDatasource() {
        var apiDatasource = ApiDatasource.create(new DatasourceId(key, version));
        // TODO(renc): other setter
        return apiDatasource;
    }
}