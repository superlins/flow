package com.zwtech.flow.config;

import com.zwtech.flow.core.plugin.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Configuration for CORS and Plugins
 *
 * @author renc
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allow credentials
        corsConfig.setAllowCredentials(true);

        // Allow specific origins
        corsConfig.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:5174"
        ));

        // Allow common HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS",
                "PATCH"
        ));

        // Allow common headers
        corsConfig.setAllowedHeaders(Arrays.asList(
                "*",
                "Content-Type",
                "Authorization",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        // Expose headers (if needed)
        corsConfig.setExposedHeaders(Arrays.asList(
                "Content-Type",
                "Authorization"
        ));

        // Cache preflight requests
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

    /**
     * SpringPluginManager bean for handling PF4J plugins
     */
    @Bean
    public SpringPluginManager springPluginManager() {
        // Default plugins directory: ./plugins
        Path pluginsRoot = Paths.get("plugins");
        return new SpringPluginManager(pluginsRoot);
    }
}
