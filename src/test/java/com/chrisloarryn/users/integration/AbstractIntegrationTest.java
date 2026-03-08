package com.chrisloarryn.users.integration;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractIntegrationTest {

    private static final boolean DOCKER_AVAILABLE = DockerClientFactory.instance().isDockerAvailable();
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            DOCKER_AVAILABLE ? new PostgreSQLContainer<>("postgres:17-alpine") : null;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    static {
        if (DOCKER_AVAILABLE) {
            POSTGRESQL_CONTAINER.start();
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        if (DOCKER_AVAILABLE) {
            registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
            registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        }
    }

    void requireDockerIfNeeded() {
        Assumptions.assumeTrue(DOCKER_AVAILABLE, "Docker is required for PostgreSQL-backed Testcontainers execution");
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM products");
        jdbcTemplate.execute("DELETE FROM phones");
        jdbcTemplate.execute("DELETE FROM users");
    }
}
