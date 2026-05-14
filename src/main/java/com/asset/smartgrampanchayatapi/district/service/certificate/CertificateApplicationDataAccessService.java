package com.asset.smartgrampanchayatapi.district.service.certificate;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class CertificateApplicationDataAccessService {

    private static final ZoneId APPLICATION_NUMBER_ZONE = ZoneId.of("Asia/Kolkata");

    private final CertificateApplicationRepository certificateApplicationRepository;
    private final CertificateTypeRepository certificateTypeRepository;
    private final CertificateIssuanceService certificateIssuanceService;
    private final ObjectMapper objectMapper;

    public CertificateApplicationDataAccessService(
            CertificateApplicationRepository certificateApplicationRepository,
            CertificateTypeRepository certificateTypeRepository,
            CertificateIssuanceService certificateIssuanceService,
            ObjectMapper objectMapper
    ) {
        this.certificateApplicationRepository = certificateApplicationRepository;
        this.certificateTypeRepository = certificateTypeRepository;
        this.certificateIssuanceService = certificateIssuanceService;
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
        app.setStaffRemarksJson(objectMapper.createArrayNode());

        certificateApplicationRepository.save(app);
        return CertificateApplicationDto.fromEntity(app, objectMapper);
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public CertificateApplicationDto approveApplication(
            UUID tenantId,
            UUID applicationId,
            UUID approverUserId,
            List<String> remarksToAppend
    ) {
        CertificateApplication app = certificateApplicationRepository
                .findByIdAndTenantId(applicationId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Certificate application not found for this tenant."
                ));
        if (app.getStatus() == CertificateApplicationStatus.APPROVED) {
            return CertificateApplicationDto.fromEntity(app, objectMapper);
        }
        if (app.getStatus() != CertificateApplicationStatus.SUBMITTED
                && app.getStatus() != CertificateApplicationStatus.PENDING_REVIEW) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Application cannot be approved from status " + app.getStatus()
            );
        }
        appendStaffRemarksInternal(app, remarksToAppend, approverUserId);
        app.setStatus(CertificateApplicationStatus.APPROVED);
        app.setApprovedAt(Instant.now());
        app.setApprovedByUserId(approverUserId);
        certificateApplicationRepository.save(app);
        certificateIssuanceService.buildAndPersistIssuedHtmlAfterApproval(tenantId, applicationId);
        return CertificateApplicationDto.fromEntity(app, objectMapper);
    }

    /**
     * Rejects the application (Gramsevak). Optional remarks are appended first. Idempotent if already REJECTED.
     */
    @Transactional(transactionManager = "districtTransactionManager")
    public CertificateApplicationDto rejectApplication(
            UUID tenantId,
            UUID applicationId,
            UUID staffUserId,
            List<String> remarksToAppend
    ) {
        CertificateApplication app = certificateApplicationRepository
                .findByIdAndTenantId(applicationId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Certificate application not found for this tenant."
                ));
        if (app.getStatus() == CertificateApplicationStatus.REJECTED) {
            return CertificateApplicationDto.fromEntity(app, objectMapper);
        }
        if (app.getStatus() == CertificateApplicationStatus.APPROVED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot reject an application that is already approved."
            );
        }
        if (app.getStatus() == CertificateApplicationStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot reject a cancelled application."
            );
        }
        if (app.getStatus() != CertificateApplicationStatus.SUBMITTED
                && app.getStatus() != CertificateApplicationStatus.PENDING_REVIEW
                && app.getStatus() != CertificateApplicationStatus.PENDING_PAYMENT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Application cannot be rejected from status " + app.getStatus()
            );
        }
        appendStaffRemarksInternal(app, remarksToAppend, staffUserId);
        app.setStatus(CertificateApplicationStatus.REJECTED);
        app.setApprovedAt(null);
        app.setApprovedByUserId(null);
        certificateApplicationRepository.save(app);
        return CertificateApplicationDto.fromEntity(app, objectMapper);
    }

    /**
     * Appends one or more staff remarks (Gramsevak). Allowed at any workflow stage except once the application is
     * approved, rejected, or cancelled ({@link #ensureStatusAllowsStaffRemarks}).
     */
    @Transactional(transactionManager = "districtTransactionManager")
    public CertificateApplicationDto appendStaffRemarks(
            UUID tenantId,
            UUID applicationId,
            List<String> texts,
            UUID authorUserId
    ) {
        CertificateApplication app = certificateApplicationRepository
                .findByIdAndTenantId(applicationId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Certificate application not found for this tenant."
                ));
        ensureStatusAllowsStaffRemarks(app.getStatus());
        appendStaffRemarksInternal(app, texts, authorUserId);
        certificateApplicationRepository.save(app);
        return CertificateApplicationDto.fromEntity(app, objectMapper);
    }

    /**
     * Staff remarks are allowed whenever the application is still actionable; they are blocked only for terminal
     * outcomes where the record should stay fixed: {@link CertificateApplicationStatus#APPROVED},
     * {@link CertificateApplicationStatus#REJECTED}, {@link CertificateApplicationStatus#CANCELLED}.
     * <p>
     * Any other current enum value (e.g. {@link CertificateApplicationStatus#SUBMITTED},
     * {@link CertificateApplicationStatus#PENDING_REVIEW}, {@link CertificateApplicationStatus#PENDING_PAYMENT})
     * allows remarks; future statuses are allowed unless added to the blocked set here.
     *
     * @param status current application status
     * @throws ResponseStatusException {@code 409 CONFLICT} when status is approved, rejected, or cancelled
     */
    private static void ensureStatusAllowsStaffRemarks(CertificateApplicationStatus status) {
        switch (status) {
            case APPROVED, REJECTED, CANCELLED -> throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Staff remarks cannot be added when status is " + status
            );
            default -> {
                // ok — submitted / pending / any future non-terminal status
            }
        }
    }

    private static final int MAX_STAFF_REMARKS_PER_APPLICATION = 100;

    private void appendStaffRemarksInternal(CertificateApplication app, List<String> rawTexts, UUID userId) {
        if (rawTexts == null || rawTexts.isEmpty()) {
            return;
        }
        ArrayNode arr = staffRemarksArray(app);
        List<String> normalized = new ArrayList<>();
        for (String raw : rawTexts) {
            if (raw == null) {
                continue;
            }
            String t = raw.trim();
            if (t.isEmpty()) {
                continue;
            }
            if (t.length() > 4000) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Each remark must be at most 4000 characters."
                );
            }
            normalized.add(t);
        }
        if (normalized.isEmpty()) {
            return;
        }
        if (normalized.size() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At most 20 remarks per request.");
        }
        if (arr.size() + normalized.size() > MAX_STAFF_REMARKS_PER_APPLICATION) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At most " + MAX_STAFF_REMARKS_PER_APPLICATION + " staff remarks per application."
            );
        }
        Instant now = Instant.now();
        String createdAt = now.toString();
        String by = userId.toString();
        for (String t : normalized) {
            ObjectNode o = objectMapper.createObjectNode();
            o.put("text", t);
            o.put("createdAt", createdAt);
            o.put("createdByUserId", by);
            arr.add(o);
        }
        app.setStaffRemarksJson(arr);
    }

    private ArrayNode staffRemarksArray(CertificateApplication app) {
        JsonNode n = app.getStaffRemarksJson();
        if (n == null || !n.isArray()) {
            return objectMapper.createArrayNode();
        }
        return (ArrayNode) n;
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
