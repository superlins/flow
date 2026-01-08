package com.zwtech.flow.connector.factory.http;

import com.zwtech.flow.connector.Connector;
import com.zwtech.flow.connector.factory.AbstractConnectorFactory;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
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
 * @author renc
 */
public class HttpConnectorFactory extends AbstractConnectorFactory<HttpRequestSpec, HttpResponseSpec> {

    private final Map<String, WebClient> clientCache = new ConcurrentHashMap<>();

    private final WebClient.Builder webClientBuilder;

    public HttpConnectorFactory(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Connector<HttpRequestSpec, HttpResponseSpec> create(ApiDatasource apiDatasource) {
        var connection = (HttpDatasourceConnection) apiDatasource.connection();
        // String clientKey = generateKey(apiDatasource);
        // var webClient = clientCache.computeIfAbsent(clientKey, key -> createWebClient(apiDatasource));
        var webClient = webClientBuilder.build();

        return new HttpConnector(webClient);
    }

    private String generateKey(ApiDatasource apiDatasource) {
        // var connection = apiDatasource.getConnection();
        // if (connection != null) {
        //     return connection.getHost() + ":" + connection.getPort() + "_" +
        //            apiDatasource.getAuthentication().hashCode();
        // }
        return "default";
    }

    private WebClient createWebClient(HttpConnectorConfig config) {
        var connectionProvider = ConnectionProvider.builder("flowHttpClient")
                .maxConnections(200).build();

        var httpClient = HttpClient.create(connectionProvider)
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
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 增加内存限制
                .build();
    }
}
