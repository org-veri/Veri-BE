package org.veri.be.slice.persistence;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;

@ActiveProfiles("persistence")
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = PersistenceSliceTestSupport.PersistenceTestConfig.class,
        initializers = ConfigDataApplicationContextInitializer.class
)
public abstract class PersistenceSliceTestSupport {

    static final MySQLContainer<?> MYSQL;

    static {
        MYSQL = new MySQLContainer<>("mysql:8.3.0")
                .withDatabaseName("smoody_test")
                .withUsername("test")
                .withPassword("test");
        MYSQL.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @TestConfiguration
    @AutoConfigurationPackage(basePackages = "org.veri.be")
    @EnableAutoConfiguration
    @EnableJpaRepositories(basePackages = "org.veri.be")
    static class PersistenceTestConfig {
    }
}
