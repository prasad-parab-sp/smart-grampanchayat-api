package com.asset.smartgrampanchayatapi.district.service.certificate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.routing.TenantCodeContext;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;
import com.asset.smartgrampanchayatapi.web.dto.CertificateDocumentFormatDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateDocumentFormatUpsertRequest;

@Service
public class CertificateDocumentFormatService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final CertificateDocumentFormatDataAccessService certificateDocumentFormatDataAccessService;

    public CertificateDocumentFormatService(
            TenantShardRoutingService tenantShardRoutingService,
            CertificateDocumentFormatDataAccessService certificateDocumentFormatDataAccessService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.certificateDocumentFormatDataAccessService = certificateDocumentFormatDataAccessService;
    }

    public List<CertificateDocumentFormatDto> list() {
        return listOrNotFound();
    }

    public Optional<CertificateDocumentFormatDto> getById(UUID id) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load certificate document format",
                ctx -> certificateDocumentFormatDataAccessService.findByIdForTenant(id, ctx.tenantId())
        );
    }

    public CertificateDocumentFormatDto create(CertificateDocumentFormatUpsertRequest body) {
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not create certificate document format",
                        ctx -> Optional.of(certificateDocumentFormatDataAccessService.create(ctx, body))
                )
                .orElseThrow(this::unknownTenant);
    }

    public CertificateDocumentFormatDto update(UUID id, CertificateDocumentFormatUpsertRequest body) {
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not update certificate document format",
                        ctx -> Optional.of(certificateDocumentFormatDataAccessService.update(ctx, id, body))
                )
                .orElseThrow(this::unknownTenant);
    }

    public void delete(UUID id) {
        tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not delete certificate document format",
                        ctx -> {
                            certificateDocumentFormatDataAccessService.deleteForTenant(ctx, id);
                            return Optional.of(Boolean.TRUE);
                        }
                )
                .orElseThrow(this::unknownTenant);
    }

    private List<CertificateDocumentFormatDto> listOrNotFound() {
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not list certificate document formats",
                        ctx -> Optional.of(certificateDocumentFormatDataAccessService.listForTenant(ctx.tenantId()))
                )
                .orElseThrow(this::unknownTenant);
    }

    private ResponseStatusException unknownTenant() {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Unknown tenant: no master DB row for tenant_code matching X-Tenant-Code."
        );
    }
}
