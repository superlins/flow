package com.zwtech.flow.connector.factory.http;

import com.zwtech.flow.connector.Connector;
import com.zwtech.flow.connector.factory.AbstractConnectorFactory;
import com.zwtech.flow.connector.specs.DatasourceSpecs;
import com.zwtech.flow.connector.specs.HttpDatasourceSpecs;
import com.zwtech.flow.domain.model.apidatasource.connection.HttpDatasourceConnection;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP 连接器工厂
 * <p>
 * 根据数据源规格创建 Connector 实例。
 * Connection 信息用于构建 WebClient 客户端。
 *
 * @author renc
 */
public class HttpConnectorFactory extends AbstractConnectorFactory<HttpRequestSpec, HttpResponseSpec> {

    private final Map<String, WebClient> clientCache = new ConcurrentHashMap<>();
    private final WebClient.Builder webClientBuilder;

    public HttpConnectorFactory(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Connector<HttpRequestSpec, HttpResponseSpec> create(DatasourceSpecs specs) {
        if (!(specs instanceof HttpDatasourceSpecs httpSpecs)) {
            throw new IllegalArgumentException("Expected HttpDatasourceSpecs, got: " + specs.getClass().getName());
        }

        HttpDatasourceConnection connection = httpSpecs.getConnection();

        // 根据连接配置构建 WebClient
        WebClient webClient = createWebClient(connection);

        return new HttpConnector(webClient);
    }

    /**
     * 根据连接配置创建 WebClient
     */
    private WebClient createWebClient(HttpDatasourceConnection connection) {
        HttpConnectorConfig config = HttpConnectorConfig.from(connection);

        var connectionProvider = ConnectionProvider.builder("flowHttpClient")
                .maxConnections(200)
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .compress(config.isCompressionEnabled())
                .disableRetry(config.isRetryDisabled())
                .responseTimeout(config.getResponseTimeout())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) config.getConnectionTimeout().toMillis());

        if (config.isCertVerifyDisabled()) {
            try {
                SslContext sslContext = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();

                httpClient = httpClient.secure(spec -> spec.sslContext(sslContext));
            } catch (Exception e) {
                LOGGER.error("Could not configure HTTP Client SSL self signed strategy", e);
            }
        }

        return webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }
}
