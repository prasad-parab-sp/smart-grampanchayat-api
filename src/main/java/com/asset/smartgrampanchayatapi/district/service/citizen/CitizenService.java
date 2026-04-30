package com.asset.smartgrampanchayatapi.district.service.citizen;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.asset.smartgrampanchayatapi.district.jpa.model.Citizen;
import com.asset.smartgrampanchayatapi.district.routing.TenantCodeContext;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;

/**
 * Application service for district-shard {@link Citizen} data (reads today; add create/update/delete here as needed).
 */
@Service
public class CitizenService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final CitizenDataAccessService citizenDataAccessService;

    public CitizenService(
            TenantShardRoutingService tenantShardRoutingService,
            CitizenDataAccessService citizenDataAccessService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.citizenDataAccessService = citizenDataAccessService;
    }

    /** Resolves tenant from {@link TenantCodeContext}, returns citizen only if {@code id} belongs to that tenant. */
    public Optional<Citizen> findById(UUID id) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load citizen from district database",
                ctx -> citizenDataAccessService.findByIdAndTenantId(ctx.district(), id, ctx.tenantId())
        );
    }

    /**
     * Uses {@link TenantCodeContext} to route, then loads the citizen by {@code mobile} or {@code email} (exactly one).
     */
    public Optional<Citizen> findByMobileOrEmail(String mobile, String email) {
        String code = TenantCodeContext.getRequired();
        if (mobile != null && !mobile.isBlank()) {
            return tenantShardRoutingService.runOnShard(
                    code,
                    "Could not load citizen from district database",
                    ctx -> citizenDataAccessService.findByTenantIdAndMobile(ctx.district(), ctx.tenantId(), mobile.trim())
            );
        }
        if (email != null && !email.isBlank()) {
            return tenantShardRoutingService.runOnShard(
                    code,
                    "Could not load citizen from district database",
                    ctx -> citizenDataAccessService.findByTenantIdAndEmailIgnoreCase(
                            ctx.district(), ctx.tenantId(), email.trim())
            );
        }
        return Optional.empty();
    }

    /** When {@code tenantCode} is already known (e.g. tests or internal calls). */
    public Optional<Citizen> findByTenantCodeAndMobile(String tenantCode, String mobile) {
        return tenantShardRoutingService.runOnShard(
                tenantCode,
                "Could not load citizen from district database",
                ctx -> citizenDataAccessService.findByTenantIdAndMobile(ctx.district(), ctx.tenantId(), mobile.trim())
        );
    }

    public Optional<Citizen> findByTenantCodeAndEmail(String tenantCode, String email) {
        return tenantShardRoutingService.runOnShard(
                tenantCode,
                "Could not load citizen from district database",
                ctx -> citizenDataAccessService.findByTenantIdAndEmailIgnoreCase(
                        ctx.district(), ctx.tenantId(), email.trim())
        );
    }

    public Optional<Citizen> findByTenantCodeAndId(String tenantCode, UUID id) {
        return tenantShardRoutingService.runOnShard(
                tenantCode,
                "Could not load citizen from district database",
                ctx -> citizenDataAccessService.findByIdAndTenantId(ctx.district(), id, ctx.tenantId())
        );
    }
}
