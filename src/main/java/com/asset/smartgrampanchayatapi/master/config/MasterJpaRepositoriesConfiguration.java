package com.asset.smartgrampanchayatapi.master.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

/**
 * Master-database JPA repositories (mirrors {@code district.jpa.repository} for shard DB).
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.asset.smartgrampanchayatapi.master.jpa.repository",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "masterTransactionManager"
)
public class MasterJpaRepositoriesConfiguration {

    /**
     * Primary TM for central DB ({@code spring.datasource}). Boot 4 does not register a {@code transactionManager} bean
     * when a second {@code districtTransactionManager} exists; master code must reference this explicitly.
     */
    @Bean(name = "masterTransactionManager")
    @Primary
    public PlatformTransactionManager masterTransactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
