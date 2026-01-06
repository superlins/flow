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
    STRICT_        BOOLEAN                  NOT NULL DEFAULT FALSE,
    OPERATION_     JSONB                    NOT NULL,
    CONNECTION_    JSONB                    NOT NULL,
    EXTENSION_     JSONB                    NOT NULL,
    CREATED_AT_    TIMESTAMP WITH TIME ZONE NOT NULL,
    UPDATED_AT_    TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (KEY_, VERSION_)
);

CREATE TABLE FLW_API_SERVICE
(
    ID_                BIGSERIAL PRIMARY KEY,
    SERVICE_ID_        VARCHAR(64)              NOT NULL UNIQUE,
    NAME_              VARCHAR(64)              NOT NULL,
    DESCRIPTION_       VARCHAR(256)             NOT NULL,
    STATUS_            VARCHAR(16)              NOT NULL,
    INPUT_SCHEMA_      JSONB                    NOT NULL,
    OUTPUT_SCHEMA_     JSONB                    NOT NULL,
    DATASOURCE_KEY_    VARCHAR(64)              NOT NULL,
    DATASOURCE_VERSION_ INT                     NOT NULL,
    BINDING_SPEC_      JSONB                    NOT NULL,
    CREATED_AT_        TIMESTAMP WITH TIME ZONE NOT NULL,
    UPDATED_AT_        TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (DATASOURCE_KEY_, DATASOURCE_VERSION_) 
        REFERENCES FLW_API_DATASOURCE(KEY_, VERSION_)
);

-- 索引：用于快速查询引用某个 Datasource 的所有 Service
CREATE INDEX IDX_API_SERVICE_DATASOURCE ON FLW_API_SERVICE(DATASOURCE_KEY_, DATASOURCE_VERSION_);