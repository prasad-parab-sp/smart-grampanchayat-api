package com.asset.smartgrampanchayatapi.district.service.tenant;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.asset.smartgrampanchayatapi.district.jpa.model.ShardTenant;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;
import com.asset.smartgrampanchayatapi.web.dto.TenantProfileDto;

/**
 * Application service for district-shard {@link ShardTenant} data (reads today; add create/update/delete here as needed).
 */
@Service
public class ShardTenantService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final ShardTenantDataAccessService shardTenantDataAccessService;
    private final DistrictOfficerNameService districtOfficerNameService;

    public ShardTenantService(
            TenantShardRoutingService tenantShardRoutingService,
            ShardTenantDataAccessService shardTenantDataAccessService,
            DistrictOfficerNameService districtOfficerNameService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.shardTenantDataAccessService = shardTenantDataAccessService;
        this.districtOfficerNameService = districtOfficerNameService;
    }

    /**
     * Resolves {@code tenantCode} on the master DB, then loads the full row from that district's {@code tenants} table.
     */
    public Optional<ShardTenant> findByTenantCode(String tenantCode) {
        return tenantShardRoutingService.runOnShard(
                tenantCode,
                "Could not load tenant from district database",
                ctx -> shardTenantDataAccessService.findByTenantCode(ctx.tenantCode())
        );
    }

    /**
     * Same as {@link #findByTenantCode(String)} plus officer display strings (sarpanch from active {@code users}, gramsevak from {@code grampanchayat}).
     */
    public Optional<TenantProfileDto> findProfileByTenantCode(String tenantCode) {
        return tenantShardRoutingService.runOnShard(
                tenantCode,
                "Could not load tenant from district database",
                ctx -> shardTenantDataAccessService
                        .findByTenantCode(ctx.tenantCode())
                        .map(t -> {
                            String sarpanchDisplay = districtOfficerNameService
                                    .resolveSarpanchDisplayName(t.getId())
                                    .orElse(null);
                            String gramsevakDisplay = districtOfficerNameService
                                    .resolveGramsevakDisplayName(t.getId())
                                    .orElse(null);
                            return TenantProfileDto.fromParts(t, sarpanchDisplay, gramsevakDisplay);
                        })
        );
    }
}
