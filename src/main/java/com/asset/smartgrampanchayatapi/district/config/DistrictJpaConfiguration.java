package com.asset.smartgrampanchayatapi.district.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.asset.smartgrampanchayatapi.district.routing.DistrictDataSourceRegistry;
import com.asset.smartgrampanchayatapi.district.routing.DistrictRoutingDataSource;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@EnableJpaRepositories(
        basePackages = "com.asset.smartgrampanchayatapi.district.jpa.repository",
        entityManagerFactoryRef = "districtEntityManagerFactory",
        transactionManagerRef = "districtTransactionManager"
)
public class DistrictJpaConfiguration {

    /**
     * Registers an {@link EntityManagerFactory} only (not {@link LocalContainerEntityManagerFactoryBean} as its own bean)
     * so {@link HibernateJpaAutoConfiguration} can still create the primary named {@code entityManagerFactory}. Depends on that
     * primary bean so it is built first; lazy so startup does not route a shard JDBC connection during metadata bootstrap when
     * possible.
     */
    /**
     * Declared as {@link Object} so {@code JpaBaseConfiguration}'s
     * {@code @ConditionalOnMissingBean(EntityManagerFactory.class)} still matches for the primary unit; the actual
     * instance is an {@link EntityManagerFactory}.
     */
    @Lazy
    @Bean(name = "districtEntityManagerFactory", destroyMethod = "close")
    @DependsOn("entityManagerFactory")
    public Object districtEntityManagerFactory(
            DistrictDataSourceRegistry districtDataSourceRegistry) throws Exception {
        // Not a Spring DataSource bean — keeps a single candidate for primary Hibernate/JPA auto-config.
        DistrictRoutingDataSource districtRoutingDataSource = new DistrictRoutingDataSource(districtDataSourceRegistry);
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(districtRoutingDataSource);
        em.setPackagesToScan("com.asset.smartgrampanchayatapi.district.jpa.model");
        em.setPersistenceUnitName("district");
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(adapter);

        Properties props = new Properties();
        props.setProperty("hibernate.hbm2ddl.auto", "none");
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.setProperty("hibernate.boot.allow_jdbc_metadata_access", "false");
        props.setProperty("hibernate.temp.use_jdbc_metadata_defaults", "false");
        em.setJpaProperties(props);

        em.afterPropertiesSet();
        return em.getObject();
    }

    @Bean(name = "districtTransactionManager")
    public PlatformTransactionManager districtTransactionManager(
            @Qualifier("districtEntityManagerFactory") Object districtEntityManagerFactory) {
        return new JpaTransactionManager((EntityManagerFactory) districtEntityManagerFactory);
    }
}
