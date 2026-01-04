CREATE TABLE api_datasource
(
    ds_key        VARCHAR NOT NULL,
    ds_version    INT     NOT NULL,
    ds_type       VARCHAR NOT NULL,
    status        VARCHAR NOT NULL,
    input_schema  TEXT    NOT NULL,
    output_schema TEXT    NOT NULL,
    strict        BOOLEAN NOT NULL,
    PRIMARY KEY (ds_key, ds_version)
);