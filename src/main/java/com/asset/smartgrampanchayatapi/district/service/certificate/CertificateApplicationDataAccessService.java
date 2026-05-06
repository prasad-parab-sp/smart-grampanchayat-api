package com.asset.smartgrampanchayatapi.district.service.certificate;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.Predicate;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateApplication;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateApplicationStatus;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CertificateApplicationRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CertificateTypeRepository;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingContext;
import com.asset.smartgrampanchayatapi.web.dto.CertificateApplicationDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateApplicationSubmitRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CertificateApplicationDataAccessService {

    private static final ZoneId APPLICATION_NUMBER_ZONE = ZoneId.of("Asia/Kolkata");

    private final CertificateApplicationRepository certificateApplicationRepository;
    private final CertificateTypeRepository certificateTypeRepository;
    private final ObjectMapper objectMapper;

    public CertificateApplicationDataAccessService(
            CertificateApplicationRepository certificateApplicationRepository,
            CertificateTypeRepository certificateTypeRepository,
            ObjectMapper objectMapper
    ) {
        this.certificateApplicationRepository = certificateApplicationRepository;
        this.certificateTypeRepository = certificateTypeRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public CertificateApplicationDto createApplication(
            TenantShardRoutingContext ctx,
            CertificateApplicationSubmitRequest request
    ) {
        if (certificateTypeRepository.findVisibleByIdForTenant(request.certificateTypeId(), ctx.tenantId()).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Certificate type is not available for this tenant or is disabled"
            );
        }

        Instant submittedAt = Instant.now();
        String tenantCodeLabel = tenantCodeForApplicationNumber(ctx);
        String applicationNumber = nextApplicationNumber(ctx.tenantId(), tenantCodeLabel, submittedAt);

        CertificateApplication app = new CertificateApplication();
        app.setId(UUID.randomUUID());
        app.setTenantId(ctx.tenantId());
        app.setCertificateTypeId(request.certificateTypeId());
        app.setApplicationNumber(applicationNumber);
        app.setApplicantFullName(request.applicantFullName().trim());
        app.setApplicantMobile(request.applicantMobile().trim());
        app.setReasonShort(trimToNull(request.reasonShort()));
        app.setReasonDetails(trimToNull(request.reasonDetails()));
        app.setAddressText(trimToNull(request.addressText()));
        app.setForWhomName(trimToNull(request.forWhomName()));
        app.setCitizenId(request.citizenId());
        app.setStatus(CertificateApplicationStatus.SUBMITTED);
        app.setSubmittedAt(submittedAt);
        app.setAdditionalValuesJson(toJson(request.additionalValues()));

        certificateApplicationRepository.save(app);
        return CertificateApplicationDto.fromEntity(app, objectMapper);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<CertificateApplicationDto> findByIdForTenant(UUID applicationId, UUID tenantId) {
        Optional<CertificateApplication> app = certificateApplicationRepository.findByIdAndTenantId(applicationId, tenantId);
        if (app.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(CertificateApplicationDto.fromEntity(app.get(), objectMapper));
    }

    /**
     * Lists applications for the tenant, newest {@code submittedAt} first.
     * Omit {@code citizenId} and/or {@code status} to skip that filter.
     */
    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public List<CertificateApplicationDto> listForTenant(
            UUID tenantId,
            UUID citizenId,
            CertificateApplicationStatus status
    ) {
        Specification<CertificateApplication> spec = (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("submittedAt")));
            Predicate match = cb.equal(root.get("tenantId"), tenantId);
            if (citizenId != null) {
                match = cb.and(match, cb.equal(root.get("citizenId"), citizenId));
            }
            if (status != null) {
                match = cb.and(match, cb.equal(root.get("status"), status));
            }
            return match;
        };
        return certificateApplicationRepository.findAll(spec).stream()
                .map(e -> CertificateApplicationDto.fromEntity(e, objectMapper))
                .toList();
    }

    private JsonNode toJson(Map<String, Object> additionalValues) {
        Map<String, Object> map = additionalValues == null ? Map.of() : additionalValues;
        JsonNode node = objectMapper.valueToTree(map);
        if (node == null || node.isNull() || !node.isObject()) {
            return objectMapper.createObjectNode();
        }
        return node;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * {@code X-Tenant-Code} (uppercase) / IST calendar year / sequence. The code segment is taken from the request
     * header after routing trim — same value {@link TenantShardRoutingContext#tenantCode()} — not re-read from the shard DB.
     * Sequence is tenant-global: {@code count(applications for tenant) + 1}. Not strictly safe under concurrent submits.
     */
    private String nextApplicationNumber(UUID tenantId, String tenantCode, Instant submittedAt) {
        int year = submittedAt.atZone(APPLICATION_NUMBER_ZONE).getYear();
        long prior = certificateApplicationRepository.countByTenantId(tenantId);
        long seq = prior + 1;
        return String.format("%s/%d/%05d", tenantCode, year, seq);
    }

    /** Uppercase prefix from {@code X-Tenant-Code} (via {@link TenantShardRoutingContext#tenantCode()}). */
    private static String tenantCodeForApplicationNumber(TenantShardRoutingContext ctx) {
        String code = ctx.tenantCode() == null ? "" : ctx.tenantCode().trim();
        return code.toUpperCase(Locale.ROOT);
    }
}
