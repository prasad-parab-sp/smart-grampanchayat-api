package com.asset.smartgrampanchayatapi.district.service.certificate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateApplicationStatus;
import com.asset.smartgrampanchayatapi.district.jpa.model.ShardUser;
import com.asset.smartgrampanchayatapi.district.jpa.model.UserRole;
import com.asset.smartgrampanchayatapi.district.routing.TenantCodeContext;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;
import com.asset.smartgrampanchayatapi.district.service.user.UserService;
import com.asset.smartgrampanchayatapi.web.dto.CertificateApplicationAddStaffRemarksRequest;
import com.asset.smartgrampanchayatapi.web.dto.CertificateApplicationApproveRequest;
import com.asset.smartgrampanchayatapi.web.dto.CertificateApplicationRejectRequest;
import com.asset.smartgrampanchayatapi.web.dto.CertificateApplicationDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateApplicationSubmitRequest;
import com.asset.smartgrampanchayatapi.web.dto.CertificateIssuedDocumentDto;

@Service
public class CertificateApplicationService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final CertificateApplicationDataAccessService certificateApplicationDataAccessService;
    private final CertificateIssuanceService certificateIssuanceService;
    private final UserService userService;

    public CertificateApplicationService(
            TenantShardRoutingService tenantShardRoutingService,
            CertificateApplicationDataAccessService certificateApplicationDataAccessService,
            CertificateIssuanceService certificateIssuanceService,
            UserService userService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.certificateApplicationDataAccessService = certificateApplicationDataAccessService;
        this.certificateIssuanceService = certificateIssuanceService;
        this.userService = userService;
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

    /**
     * Gramsevak-only: verifies staff credentials on the shard, then marks the application APPROVED.
     * Allowed from {@link CertificateApplicationStatus#SUBMITTED} or {@link CertificateApplicationStatus#PENDING_REVIEW}.
     * Idempotent when already {@link CertificateApplicationStatus#APPROVED}.
     */
    public CertificateApplicationDto approve(UUID applicationId, CertificateApplicationApproveRequest body) {
        String tenantCode = TenantCodeContext.getRequired();
        ShardUser staff = userService
                .verifyActiveUserCredentials(body.identifier(), body.password())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid credentials."
                ));
        if (staff.getRole() != UserRole.GRAMSEVAK) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only Gramsevak may approve certificate applications."
            );
        }
        return tenantShardRoutingService
                .runOnShard(
                        tenantCode,
                        "Could not approve certificate application",
                        ctx -> Optional.of(certificateApplicationDataAccessService.approveApplication(
                                ctx.tenantId(),
                                applicationId,
                                staff.getId(),
                                normalizeRemarkLines(body.remarksToAppend())
                        ))
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant: no master DB row for tenant_code matching X-Tenant-Code: " + tenantCode
                                + ". Use GET /api/tenants?tenantCode=... to verify, or insert tenants on master."
                ));
    }

    /**
     * Gramsevak-only: rejects the application (not approved / not cancelled). Optional remark lines appended first.
     */
    public CertificateApplicationDto reject(UUID applicationId, CertificateApplicationRejectRequest body) {
        String tenantCode = TenantCodeContext.getRequired();
        ShardUser staff = userService
                .verifyActiveUserCredentials(body.identifier(), body.password())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid credentials."
                ));
        if (staff.getRole() != UserRole.GRAMSEVAK) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only Gramsevak may reject certificate applications."
            );
        }
        return tenantShardRoutingService
                .runOnShard(
                        tenantCode,
                        "Could not reject certificate application",
                        ctx -> Optional.of(certificateApplicationDataAccessService.rejectApplication(
                                ctx.tenantId(),
                                applicationId,
                                staff.getId(),
                                normalizeRemarkLines(body.remarksToAppend())
                        ))
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant: no master DB row for tenant_code matching X-Tenant-Code: " + tenantCode
                                + ". Use GET /api/tenants?tenantCode=... to verify, or insert tenants on master."
                ));
    }

    /**
     * Gramsevak-only: appends one or more remarks (same credential check as approve).
     */
    public CertificateApplicationDto appendStaffRemarks(UUID applicationId, CertificateApplicationAddStaffRemarksRequest body) {
        String tenantCode = TenantCodeContext.getRequired();
        ShardUser staff = userService
                .verifyActiveUserCredentials(body.identifier(), body.password())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid credentials."
                ));
        if (staff.getRole() != UserRole.GRAMSEVAK) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only Gramsevak may add staff remarks."
            );
        }
        List<String> lines = normalizeRemarkLines(body.texts());
        if (lines.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one non-blank remark line is required.");
        }
        return tenantShardRoutingService
                .runOnShard(
                        tenantCode,
                        "Could not append staff remarks",
                        ctx -> Optional.of(certificateApplicationDataAccessService.appendStaffRemarks(
                                ctx.tenantId(),
                                applicationId,
                                lines,
                                staff.getId()
                        ))
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant: no master DB row for tenant_code matching X-Tenant-Code: " + tenantCode
                                + ". Use GET /api/tenants?tenantCode=... to verify, or insert tenants on master."
                ));
    }

    private static List<String> normalizeRemarkLines(List<String> raw) {
        if (raw == null) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String s : raw) {
            if (s == null) {
                continue;
            }
            String t = s.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return List.copyOf(out);
    }

    /**
     * Citizen-only: returns frozen or freshly-built printable HTML for an {@link com.asset.smartgrampanchayatapi.district.jpa.model.CertificateApplicationStatus#APPROVED}
     * application when {@code citizenId} matches the row.
     */
    public CertificateIssuedDocumentDto getIssuedDocument(UUID applicationId, UUID citizenId, String lang) {
        String tenantCode = TenantCodeContext.getRequired();
        return tenantShardRoutingService
                .runOnShard(
                        tenantCode,
                        "Could not load issued certificate",
                        ctx -> Optional.of(certificateIssuanceService.loadOrBuildIssuedDocument(ctx, applicationId, citizenId, lang))
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant: no master DB row for tenant_code matching X-Tenant-Code: " + tenantCode
                                + ". Use GET /api/tenants?tenantCode=... to verify, or insert tenants on master."
                ));
    }
}
