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

/**
 * Reads and writes {@code certificate_document_format} on the <strong>district shard</strong> (see
 * {@link com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService} and
 * {@code master.districts} JDBC fields). This is <em>not</em> the primary {@code spring.datasource} database.
 */
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
        ensureSingleFormatPerCertificateType(ctx.tenantId(), certTypeId, null);
        CertificateDocumentFormat row = new CertificateDocumentFormat();
        row.setId(UUID.randomUUID());
        row.setTenantId(ctx.tenantId());
        row.setCertificateTypeId(certTypeId);
        applyUpsert(row, req, now, true);
        formatRepository.saveAndFlush(row);
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
        ensureSingleFormatPerCertificateType(ctx.tenantId(), certTypeId, id);
        row.setCertificateTypeId(certTypeId);
        applyUpsert(row, req, Instant.now(), false);
       formatRepository.saveAndFlush(row);
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

    /**
     * Enforces at most one {@link CertificateDocumentFormat} per (tenant, certificate type) when a type is set.
     * If {@code certificateTypeId} is {@code null}, no check runs (format not tied to a specific type).
     * <p>
     * On update, pass the row being saved as {@code exceptFormatId} so the existing assignment does not count
     * as a duplicate; another row using the same type still triggers conflict.
     *
     * @param tenantId          tenant whose formats are checked
     * @param certificateTypeId resolved {@link CertificateType} id, or {@code null} to skip
     * @param exceptFormatId    format id to exclude from the duplicate search, or {@code null} on create
     * @throws ResponseStatusException {@code 409 CONFLICT} when another format already uses this certificate type
     */
    private void ensureSingleFormatPerCertificateType(UUID tenantId, UUID certificateTypeId, UUID exceptFormatId) {
        if (certificateTypeId == null) {
            return;
        }
        boolean conflict = exceptFormatId == null
                ? formatRepository.existsByTenantIdAndCertificateTypeId(tenantId, certificateTypeId)
                : formatRepository.existsByTenantIdAndCertificateTypeIdAndIdNotIn(
                        tenantId,
                        certificateTypeId,
                        List.of(exceptFormatId)
                );
        if (conflict) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This certificate type already has a format for this tenant. Edit that format instead of creating another."
            );
        }
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
