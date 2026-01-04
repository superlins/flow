CREATE TABLE FLW_API_DATASOURCE
(
    ID_            BIGSERIAL PRIMARY KEY,
    KEY_           VARCHAR(64)              NOT NULL,
    VERSION_       INT                      NOT NULL,
    TYPE_          VARCHAR(32)              NOT NULL,
    STATUS_        VARCHAR(16)              NOT NULL,
    NAME_          VARCHAR(64)              NOT NULL,
    DESCRIPTION_   VARCHAR(256)             NOT NULL,
    INPUT_SCHEMA_  JSONB                    NOT NULL,
    OUTPUT_SCHEMA_ JSONB                    NOT NULL,
    OPERATION_     JSONB                    NOT NULL,
    CONNECTION_    JSONB                    NOT NULL,
    EXTENSION_     JSONB                    NOT NULL,
    CREATED_AT_    TIMESTAMP WITH TIME ZONE NOT NULL,
    UPDATED_AT_    TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (KEY_, VERSION_)
);