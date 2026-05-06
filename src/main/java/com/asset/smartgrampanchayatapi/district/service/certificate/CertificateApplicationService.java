package com.asset.smartgrampanchayatapi.district.service.certificate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateApplicationStatus;
import com.asset.smartgrampanchayatapi.district.routing.TenantCodeContext;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;
import com.asset.smartgrampanchayatapi.web.dto.CertificateApplicationDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateApplicationSubmitRequest;

@Service
public class CertificateApplicationService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final CertificateApplicationDataAccessService certificateApplicationDataAccessService;

    public CertificateApplicationService(
            TenantShardRoutingService tenantShardRoutingService,
            CertificateApplicationDataAccessService certificateApplicationDataAccessService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.certificateApplicationDataAccessService = certificateApplicationDataAccessService;
    }

    /**
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} when {@code X-Tenant-Code} does not match
     *         {@code grampanchayat_master.tenants.tenant_code} (or tenant has no district).
     */
    public CertificateApplicationDto submit(CertificateApplicationSubmitRequest request) {
        String tenantCode = TenantCodeContext.getRequired();
        return tenantShardRoutingService
                .runOnShard(
                        tenantCode,
                        "Could not save certificate application",
                        ctx -> Optional.of(certificateApplicationDataAccessService.createApplication(ctx, request))
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant: no master DB row for tenant_code matching X-Tenant-Code: " + tenantCode
                                + ". Use GET /api/tenants?tenantCode=... to verify, or insert tenants on master."
                ));
    }

    public Optional<CertificateApplicationDto> getById(UUID applicationId) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load certificate application",
                ctx -> certificateApplicationDataAccessService.findByIdForTenant(applicationId, ctx.tenantId())
        );
    }

    /**
     * Lists applications for {@code X-Tenant-Code}, optionally filtered by citizen and/or status.
     *
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} when tenant code does not resolve.
     */
    public List<CertificateApplicationDto> list(UUID citizenIdFilter, CertificateApplicationStatus statusFilter) {
        String tenantCode = TenantCodeContext.getRequired();
        return tenantShardRoutingService
                .runOnShard(
                        tenantCode,
                        "Could not list certificate applications",
                        ctx -> Optional.of(certificateApplicationDataAccessService.listForTenant(
                                ctx.tenantId(),
                                citizenIdFilter,
                                statusFilter
                        ))
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant: no master DB row for tenant_code matching X-Tenant-Code: " + tenantCode
                                + ". Use GET /api/tenants?tenantCode=... to verify, or insert tenants on master."
                ));
    }
}
