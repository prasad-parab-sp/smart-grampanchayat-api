package com.asset.smartgrampanchayatapi.district.service.tenant;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.asset.smartgrampanchayatapi.district.jpa.model.Grampanchayat;
import com.asset.smartgrampanchayatapi.district.jpa.model.ShardTenant;
import com.asset.smartgrampanchayatapi.district.jpa.model.ShardUser;
import com.asset.smartgrampanchayatapi.district.jpa.model.UserRole;
import com.asset.smartgrampanchayatapi.district.jpa.repository.GrampanchayatRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.ShardUserRepository;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;
import com.asset.smartgrampanchayatapi.web.dto.TenantProfileDto;

/**
 * Application service for district-shard {@link ShardTenant} data (reads today; add create/update/delete here as needed).
 */
@Service
public class ShardTenantService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final ShardTenantDataAccessService shardTenantDataAccessService;
    private final GrampanchayatRepository grampanchayatRepository;
    private final ShardUserRepository shardUserRepository;

    public ShardTenantService(
            TenantShardRoutingService tenantShardRoutingService,
            ShardTenantDataAccessService shardTenantDataAccessService,
            GrampanchayatRepository grampanchayatRepository,
            ShardUserRepository shardUserRepository
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.shardTenantDataAccessService = shardTenantDataAccessService;
        this.grampanchayatRepository = grampanchayatRepository;
        this.shardUserRepository = shardUserRepository;
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
                            String sarpanchDisplay = shardUserRepository
                                    .findByTenantIdAndRoleAndActiveTrue(t.getId(), UserRole.SARPANCH)
                                    .map(ShardTenantService::displayNameFromUser)
                                    .orElse(null);
                            String gramsevakDisplay = grampanchayatRepository
                                    .findByTenantId(t.getId())
                                    .map(Grampanchayat::getGramsevakName)
                                    .orElse(null);
                            return TenantProfileDto.fromParts(t, sarpanchDisplay, gramsevakDisplay);
                        })
        );
    }

    private static String displayNameFromUser(ShardUser u) {
        String f = u.getFirstName() != null ? u.getFirstName().trim() : "";
        String l = u.getLastName() != null ? u.getLastName().trim() : "";
        String joined = (f + " " + l).trim();
        return joined.isEmpty() ? null : joined;
    }
}
