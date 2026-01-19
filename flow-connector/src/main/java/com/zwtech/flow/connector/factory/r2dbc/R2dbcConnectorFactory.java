package com.zwtech.flow.connector.factory.r2dbc;

import com.zwtech.flow.connector.Connector;
import com.zwtech.flow.connector.factory.AbstractConnectorFactory;
import com.zwtech.flow.connector.factory.ConnectorFactory;
import com.zwtech.flow.connector.specs.DatasourceSpecs;
import com.zwtech.flow.connector.specs.R2dbcDatasourceSpecs;
import com.zwtech.flow.domain.model.apidatasource.connection.R2DbcDatasourceConnection;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.ConnectionFactories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

/**
 * R2DBC 连接器工厂
 * <p>
 * 根据数据源规格创建 Connector 实例。
 * Connection 信息用于构建 DatabaseClient 客户端。
 *
 * @author renc
 */
@Component
public class R2dbcConnectorFactory extends AbstractConnectorFactory<R2dbcRequestSpec, R2dbcResponseSpec>
        implements ConnectorFactory<R2dbcRequestSpec, R2dbcResponseSpec> {

    private static final Logger LOGGER = LoggerFactory.getLogger(R2dbcConnectorFactory.class);

    private final Map<String, DatabaseClient> clientCache = new ConcurrentHashMap<>();

    public R2dbcConnectorFactory() {
    }

    @Override
    public Connector<R2dbcRequestSpec, R2dbcResponseSpec> create(DatasourceSpecs specs) {
        if (!(specs instanceof R2dbcDatasourceSpecs r2dbcSpecs)) {
            throw new IllegalArgumentException("Expected R2dbcDatasourceSpecs, got: " + specs.getClass().getName());
        }

        R2DbcDatasourceConnection connection = r2dbcSpecs.getConnection();

        // 根据连接配置构建 DatabaseClient
        DatabaseClient databaseClient = createDatabaseClient(connection);

        return new R2dbcConnector(databaseClient);
    }

    /**
     * 根据连接配置创建 DatabaseClient
     */
    private DatabaseClient createDatabaseClient(R2DbcDatasourceConnection connection) {
        // 使用连接配置的唯一标识作为缓存键
        String cacheKey = buildCacheKey(connection);

        return clientCache.computeIfAbsent(cacheKey, key -> {
            ConnectionFactory connectionFactory = createConnectionFactory(connection);

            // 如果需要连接池，则创建 ConnectionPool
            if (connection.isPoolingEnabled()) {
                ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
                        .maxIdleTime(Duration.ofMillis(connection.getMaxIdleTime()))
                        .maxLifeTime(Duration.ofMillis(connection.getMaxLifetime()))
                        .maxSize(connection.getMaxPoolSize())
                        .initialSize(connection.getInitialPoolSize())
                        .build();

                connectionFactory = new ConnectionPool(poolConfig);
                LOGGER.info("Created R2DBC connection pool: size={}, host={}",
                        connection.getMaxPoolSize(), connection.host());
            } else {
                LOGGER.info("Created R2DBC connection (no pooling): host={}", connection.host());
            }

            return DatabaseClient.builder()
                    .connectionFactory(connectionFactory)
                    .build();
        });
    }

    /**
     * 根据连接配置创建 ConnectionFactory
     */
    private ConnectionFactory createConnectionFactory(R2DbcDatasourceConnection connection) {
        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                .option(DRIVER, connection.driver())
                .option(HOST, connection.host())
                .option(PORT, connection.port())
                .option(USER, connection.username())
                .option(PASSWORD, connection.password())
                .option(DATABASE, connection.database())
                .build();

        return ConnectionFactories.get(options);
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(R2DbcDatasourceConnection connection) {
        return String.format("%s:%s@%s:%d/%s",
                connection.driver(),
                connection.username(),
                connection.host(),
                connection.port(),
                connection.database());
    }
}
