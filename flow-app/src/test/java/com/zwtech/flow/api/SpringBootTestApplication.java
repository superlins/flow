package com.zwtech.flow.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic Spring Boot Integration Test
 * Tests that the application context loads successfully
 *
 * Note: Full API integration tests require database migration setup.
 * See ARCHITECTURE.md for details on database initialization.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootTestApplication {

    @Autowired(required = false)
    private WebTestClient webTestClient;

    @Test
    void contextLoads() {
        assertThat(webTestClient).isNotNull();
    }

    @Test
    void actuatorHealthEndpoint() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }
}
