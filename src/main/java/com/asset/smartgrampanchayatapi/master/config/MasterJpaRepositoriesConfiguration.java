package com.asset.smartgrampanchayatapi.master.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Master-database JPA repositories (mirrors {@code district.jpa.repository} for shard DB).
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.asset.smartgrampanchayatapi.master.jpa.repository")
public class MasterJpaRepositoriesConfiguration {
}
