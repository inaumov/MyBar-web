package mybar.context;

import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;

@ContextConfiguration(initializers = {
        DbTestContext.Initializer.class
})
@DataJpaTest
@TestPropertySource("classpath:application-test.yaml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class DbTestContext {

    @Autowired
    protected TestEntityManager testEntityManager;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected int countRowsInTable(String tableName) {
        return JdbcTestUtils.countRowsInTable(this.jdbcTemplate, tableName);
    }

    protected void commit() {
        testEntityManager.getEntityManager().getTransaction().commit();
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.6-alpine")
                .withReuse(true)
                .withUsername("postgres")
                .withPassword("postgres");

        private static void startContainers() {
            Startables
                    .deepStart(Stream.of(postgres))
                    .join();
        }

        private static Map<String, Object> createConnectionConfiguration() {
            return Map.of(
                    "tc.postgres.url", postgres.getJdbcUrl(),
                    "tc.postgres.username", postgres.getUsername(),
                    "tc.postgres.password", postgres.getPassword(),
                    "tc.postgres.database-name", postgres.getDatabaseName(),
                    "tc.postgres.validation-query", postgres.getTestQueryString()
            );
        }

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            startContainers();
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            MapPropertySource testcontainers = new MapPropertySource(
                    "testcontainers", createConnectionConfiguration()
            );
            environment.getPropertySources().addFirst(testcontainers);
        }
    }

    @TestConfiguration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackages = "mybar.repository")
    @EntityScan(basePackages = "mybar.domain")
    static class Config {

        @Bean
        @Primary
        public DataSource dataSource(Environment environment) {
            final DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(environment.getProperty("tc.postgres.url"));
            dataSource.setUsername(environment.getProperty("tc.postgres.username"));
            dataSource.setPassword(environment.getProperty("tc.postgres.password"));
            dataSource.setSchema(environment.getProperty("tc.postgres.database-name"));

            Awaitility.await().pollInterval(Duration.ofMillis(500L))
                    .atMost(Duration.ofSeconds(20L))
                    .until(() -> {
                        try (final Connection c = dataSource.getConnection()) {
                            c.prepareStatement(environment.getProperty("tc.postgres.validation-query"));
                            return true;
                        } catch (SQLException e) {
                            return false;
                        }
                    });
            return dataSource;
        }

        @Bean
        public JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
            final JdbcTemplate jdbcTemplate = new JdbcTemplate();
            jdbcTemplate.setDataSource(dataSource);
            return jdbcTemplate;
        }
    }

}
