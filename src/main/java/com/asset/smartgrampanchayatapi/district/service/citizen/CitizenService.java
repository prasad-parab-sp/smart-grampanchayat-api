package com.asset.smartgrampanchayatapi.district.service.citizen;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.Citizen;
import com.asset.smartgrampanchayatapi.district.jpa.model.GenderType;
import com.asset.smartgrampanchayatapi.district.routing.TenantCodeContext;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;
import com.asset.smartgrampanchayatapi.district.service.user.UserService;
import com.asset.smartgrampanchayatapi.web.dto.CitizenCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.CitizenDto;
import com.asset.smartgrampanchayatapi.web.dto.CitizenRegisterResponse;
import com.asset.smartgrampanchayatapi.web.dto.CitizenUpdateRequest;

/**
 * Application service for district-shard {@link Citizen} data.
 */
@Service
public class CitizenService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final CitizenDataAccessService citizenDataAccessService;
    private final UserService userService;

    public CitizenService(
            TenantShardRoutingService tenantShardRoutingService,
            CitizenDataAccessService citizenDataAccessService,
            UserService userService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.citizenDataAccessService = citizenDataAccessService;
        this.userService = userService;
    }

    /** Resolves tenant from {@link TenantCodeContext}, returns citizen only if {@code id} belongs to that tenant. */
    public Optional<Citizen> findById(UUID id) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load citizen from district database",
                ctx -> citizenDataAccessService.findByIdAndTenantId(id, ctx.tenantId())
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
                    ctx -> citizenDataAccessService.findByTenantIdAndMobile(ctx.tenantId(), mobile.trim())
            );
        }
        if (email != null && !email.isBlank()) {
            return tenantShardRoutingService.runOnShard(
                    code,
                    "Could not load citizen from district database",
                    ctx -> citizenDataAccessService.findByTenantIdAndEmailIgnoreCase(
                            ctx.tenantId(), email.trim())
            );
        }
        return Optional.empty();
    }

    /** When {@code tenantCode} is already known (e.g. tests or internal calls). */
    public Optional<Citizen> findByTenantCodeAndMobile(String tenantCode, String mobile) {
        return tenantShardRoutingService.runOnShard(
                tenantCode,
                "Could not load citizen from district database",
                ctx -> citizenDataAccessService.findByTenantIdAndMobile(ctx.tenantId(), mobile.trim())
        );
    }

    public Optional<Citizen> findByTenantCodeAndEmail(String tenantCode, String email) {
        return tenantShardRoutingService.runOnShard(
                tenantCode,
                "Could not load citizen from district database",
                ctx -> citizenDataAccessService.findByTenantIdAndEmailIgnoreCase(
                        ctx.tenantId(), email.trim())
        );
    }

    public Optional<Citizen> findByTenantCodeAndId(String tenantCode, UUID id) {
        return tenantShardRoutingService.runOnShard(
                tenantCode,
                "Could not load citizen from district database",
                ctx -> citizenDataAccessService.findByIdAndTenantId(id, ctx.tenantId())
        );
    }

    public Optional<CitizenRegisterResponse> listRegister(
            String search,
            GenderType gender,
            CitizenRegisterFilter filter
    ) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load citizens from district database",
                ctx -> Optional.of(
                        citizenDataAccessService.listRegister(ctx.tenantId(), search, gender, filter)
                )
        );
    }

    public CitizenDto createCitizen(CitizenCreateRequest request) {
        userService.verifyActiveStaffForCitizenRegisterWrite(request.staffUserId());
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not create citizen on district database",
                        ctx -> Optional.of(
                                citizenDataAccessService.insertCitizen(ctx, request.citizen())
                        )
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }

    public CitizenDto updateCitizen(UUID id, CitizenUpdateRequest request) {
        userService.verifyActiveStaffForCitizenRegisterWrite(request.staffUserId());
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not update citizen on district database",
                        ctx -> Optional.of(
                                citizenDataAccessService.updateCitizen(ctx.tenantId(), id, request.citizen())
                        )
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }
}
