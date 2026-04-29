package com.asset.smartgrampanchayatapi.district.routing;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import com.asset.smartgrampanchayatapi.district.config.DistrictShardProperties;
import com.asset.smartgrampanchayatapi.master.jpa.model.District;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Caches one Hikari pool per district id, built from {@link District} connection fields on the master database.
 */
@Component
public class DistrictDataSourceRegistry implements DisposableBean {

    private final DistrictShardProperties shardProperties;
    private final Map<UUID, HikariDataSource> cache = new ConcurrentHashMap<>();

    public DistrictDataSourceRegistry(DistrictShardProperties shardProperties) {
        this.shardProperties = shardProperties;
    }

    public DataSource getDataSource(District district) {
        UUID id = district.getId();
        return cache.computeIfAbsent(id, ignored -> createPool(district));
    }

    /**
     * Call after updating {@code districts} connection fields so the next {@link #getDataSource} rebuilds the pool.
     */
    public void invalidate(UUID districtId) {
        HikariDataSource removed = cache.remove(districtId);
        if (removed != null) {
            removed.close();
        }
    }

    private HikariDataSource createPool(District district) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DistrictJdbcUrlBuilder.buildUrl(district));
        config.setUsername(district.getDbUsername());
        config.setPassword(district.getDbPassword());
        config.setMaximumPoolSize(shardProperties.getMaximumPoolSize());
        config.setConnectionTimeout(shardProperties.getConnectionTimeoutMs());
        config.setPoolName("district-" + district.getId());
        return new HikariDataSource(config);
    }

    @Override
    public void destroy() {
        cache.values().forEach(HikariDataSource::close);
        cache.clear();
    }
}
