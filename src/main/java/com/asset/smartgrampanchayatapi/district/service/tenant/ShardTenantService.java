package com.asset.smartgrampanchayatapi.district.service.tenant;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.asset.smartgrampanchayatapi.district.jpa.model.ShardTenant;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;

/**
 * Application service for district-shard {@link ShardTenant} data (reads today; add create/update/delete here as needed).
 */
@Service
public class ShardTenantService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final ShardTenantDataAccessService shardTenantDataAccessService;

    public ShardTenantService(
            TenantShardRoutingService tenantShardRoutingService,
            ShardTenantDataAccessService shardTenantDataAccessService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.shardTenantDataAccessService = shardTenantDataAccessService;
    }

    /**
     * Resolves {@code tenantCode} on the master DB, then loads the full row from that district's {@code tenants} table.
     */
    public Optional<ShardTenant> findByTenantCode(String tenantCode) {
        return tenantShardRoutingService.runOnShard(
                tenantCode,
                "Could not load tenant from district database",
                ctx -> shardTenantDataAccessService.findByTenantCode(ctx.district(), ctx.tenantCode())
        );
    }
}
