package com.asset.smartgrampanchayatapi.district.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.district-shard.datasource")
public class DistrictShardProperties {

    /**
     * Max pool size per district shard (each district gets its own Hikari pool).
     */
    private int maximumPoolSize = 5;

    /**
     * Hikari connection timeout in milliseconds.
     */
    private long connectionTimeoutMs = 15000;

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public long getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(long connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }
}
