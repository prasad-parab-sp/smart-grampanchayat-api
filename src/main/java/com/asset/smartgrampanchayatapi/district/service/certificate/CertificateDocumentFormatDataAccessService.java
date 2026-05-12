package com.asset.smartgrampanchayatapi.district.service.certificate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateDocumentFormat;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateType;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CertificateDocumentFormatRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CertificateTypeRepository;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingContext;
import com.asset.smartgrampanchayatapi.web.dto.CertificateDocumentFormatDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateDocumentFormatUpsertRequest;

@Service
public class CertificateDocumentFormatDataAccessService {

    private final CertificateDocumentFormatRepository formatRepository;
    private final CertificateTypeRepository certificateTypeRepository;

    public CertificateDocumentFormatDataAccessService(
            CertificateDocumentFormatRepository formatRepository,
            CertificateTypeRepository certificateTypeRepository
    ) {
        this.formatRepository = formatRepository;
        this.certificateTypeRepository = certificateTypeRepository;
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public List<CertificateDocumentFormatDto> listForTenant(UUID tenantId) {
        return formatRepository.findByTenantIdOrderByUpdatedAtDesc(tenantId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<CertificateDocumentFormatDto> findByIdForTenant(UUID id, UUID tenantId) {
        return formatRepository.findByIdAndTenantId(id, tenantId).map(this::toDto);
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public CertificateDocumentFormatDto create(TenantShardRoutingContext ctx, CertificateDocumentFormatUpsertRequest req) {
        Instant now = Instant.now();
        UUID certTypeId = resolveCertificateTypeIdOrNull(ctx.tenantId(), req.certificateTypeCode());
        CertificateDocumentFormat row = new CertificateDocumentFormat();
        row.setId(UUID.randomUUID());
        row.setTenantId(ctx.tenantId());
        row.setCertificateTypeId(certTypeId);
        applyUpsert(row, req, now, true);
        formatRepository.save(row);
        return toDto(row);
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public CertificateDocumentFormatDto update(
            TenantShardRoutingContext ctx,
            UUID id,
            CertificateDocumentFormatUpsertRequest req
    ) {
        CertificateDocumentFormat row = formatRepository
                .findByIdAndTenantId(id, ctx.tenantId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Certificate format not found for this tenant."
                ));
        UUID certTypeId = resolveCertificateTypeIdOrNull(ctx.tenantId(), req.certificateTypeCode());
        row.setCertificateTypeId(certTypeId);
        applyUpsert(row, req, Instant.now(), false);
        formatRepository.save(row);
        return toDto(row);
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public void deleteForTenant(TenantShardRoutingContext ctx, UUID id) {
        CertificateDocumentFormat row = formatRepository
                .findByIdAndTenantId(id, ctx.tenantId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Certificate format not found for this tenant."
                ));
        formatRepository.delete(row);
    }

    private void applyUpsert(
            CertificateDocumentFormat row,
            CertificateDocumentFormatUpsertRequest req,
            Instant now,
            boolean isCreate
    ) {
        row.setDisplayName(req.name().trim());
        row.setFormatKind(req.formatKind());
        row.setDocumentTitle(trimToNull(req.documentTitle()));
        row.setBodyHtml(req.bodyHtml().trim());
        row.setFooterNote(trimToNull(req.footerNote()));
        row.setInternalNote(trimToNull(req.internalNote()));
        row.setActive(req.active());
        if (isCreate) {
            row.setCreatedAt(now);
        }
        row.setUpdatedAt(now);
    }

    private UUID resolveCertificateTypeIdOrNull(UUID tenantId, String rawCode) {
        String code = rawCode == null ? "" : rawCode.trim();
        if (code.isEmpty()) {
            return null;
        }
        return certificateTypeRepository
                .findVisibleByCodeForTenant(code, tenantId)
                .map(CertificateType::getId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Certificate type code is not available for this tenant: " + code
                ));
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private CertificateDocumentFormatDto toDto(CertificateDocumentFormat row) {
        String certCode = null;
        if (row.getCertificateTypeId() != null) {
            certCode = certificateTypeRepository
                    .findById(row.getCertificateTypeId())
                    .map(CertificateType::getCode)
                    .orElse(null);
        }
        return new CertificateDocumentFormatDto(
                row.getId(),
                row.getDisplayName(),
                row.getFormatKind(),
                row.getCertificateTypeId(),
                certCode,
                row.getDocumentTitle(),
                row.getBodyHtml(),
                row.getFooterNote(),
                row.getInternalNote(),
                row.isActive(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }
}
