package com.zwtech.flow.config;

import com.zwtech.flow.connector.factory.http.HttpConnectorAdapter;
import com.zwtech.flow.connector.factory.http.HttpConnectorFactory;
import com.zwtech.flow.connector.factory.r2dbc.R2dbcConnectorAdapter;
import com.zwtech.flow.connector.factory.r2dbc.R2dbcConnectorFactory;
import com.zwtech.flow.core.plugin.SpringPluginManager;
import com.zwtech.flow.domain.service.SchemaValidationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * HTTP Connector 自动配置类
 * <p>
 * 按照 Spring Boot 自动配置风格，条件性地创建 Connector 相关 Bean。
 *
 * @author renc
 */
@Configuration
public class ConnectorAutoConfiguration {

    /**
     * WebClient.Builder bean for HTTP clients
     */
    @Bean
    @ConditionalOnMissingBean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * HttpConnectorFactory bean for creating HTTP connectors
     */
    @Bean
    @ConditionalOnBean(WebClient.Builder.class)
    public HttpConnectorFactory httpConnectorFactory(WebClient.Builder webClientBuilder) {
        return new HttpConnectorFactory(webClientBuilder);
    }

    /**
     * HttpConnectorAdapter bean for executing HTTP datasource operations
     * <p>
     * 使用依赖注入配置请求绑定器和响应转换器。 标记为 @Primary，使其成为默认的 ConnectorAdapter 实现。
     */
    @Bean
    public HttpConnectorAdapter httpConnectorAdapter(SpringPluginManager pluginManager, ApplicationContext applicationContext,
            HttpConnectorFactory httpConnectorFactory, SchemaValidationService schemaValidationService) {
        return new HttpConnectorAdapter(pluginManager, applicationContext, httpConnectorFactory, schemaValidationService);
    }

    @Bean
    public R2dbcConnectorAdapter r2dbcConnectorAdapter(SpringPluginManager pluginManager, ApplicationContext applicationContext,
            R2dbcConnectorFactory r2dbcConnectorFactory, SchemaValidationService schemaValidationService) {
        return new R2dbcConnectorAdapter(pluginManager, applicationContext, r2dbcConnectorFactory, schemaValidationService);
    }
}
