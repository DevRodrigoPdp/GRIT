package grit.sistema.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> postgres;

    static {
        // Forzamos el host
        System.setProperty("DOCKER_HOST", "tcp://127.0.0.1:2375");

        // Esta línea es nueva: obliga a usar el transporte HTTP e ignora los Pipes
        System.setProperty("org.testcontainers.dockerclient.transport.type", "httpclient5");

        // Bloqueamos a Testcontainers para que no intente buscar otras formas de conectar
        System.setProperty("testcontainers.docker.client.strategy", "org.testcontainers.dockerclient.EnvironmentAndSystemPropertyClientProviderStrategy");

        System.setProperty("TESTCONTAINERS_RYUK_DISABLED", "true");

        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"))
                .withDatabaseName("grit_db_test")
                .withUsername("test")
                .withPassword("test");

        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }
}