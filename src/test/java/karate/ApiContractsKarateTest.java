package karate;

import java.util.Arrays;

import com.chrisloarryn.users.UsersApplication;
import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = UsersApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("karate")
class ApiContractsKarateTest {

    private static final boolean DOCKER_AVAILABLE = DockerClientFactory.instance().isDockerAvailable();
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            DOCKER_AVAILABLE ? new PostgreSQLContainer<>("postgres:17-alpine") : null;

    @LocalServerPort
    private int port;

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

    @BeforeAll
    void configureBaseUrl() {
        System.setProperty("karate.baseUrl", "http://127.0.0.1:" + port);
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM products");
        jdbcTemplate.execute("DELETE FROM phones");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Karate.Test
    Karate contracts() {
        Karate runner = new Karate().path("classpath:karate/contracts");
        String configuredTags = System.getProperty("karate.tags");
        if (configuredTags == null || configuredTags.isBlank()) {
            return runner.tags("@regression");
        }
        return runner.tags(Arrays.stream(configuredTags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .toArray(String[]::new));
    }
}
