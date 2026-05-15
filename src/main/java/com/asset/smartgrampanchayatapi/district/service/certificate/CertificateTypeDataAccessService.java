package com.asset.smartgrampanchayatapi.district.service.certificate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateType;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeCategory;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeField;
import com.asset.smartgrampanchayatapi.district.jpa.model.TenantCertificateTypeConfig;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CertificateTypeFieldRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CertificateTypeRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.TenantCertificateTypeConfigRepository;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingContext;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeFieldDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeFieldUpsertRequest;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeUpsertRequest;

@Service
public class CertificateTypeDataAccessService {

    private final CertificateTypeRepository certificateTypeRepository;
    private final TenantCertificateTypeConfigRepository tenantCertificateTypeConfigRepository;
    private final CertificateTypeFieldRepository certificateTypeFieldRepository;

    public CertificateTypeDataAccessService(
            CertificateTypeRepository certificateTypeRepository,
            TenantCertificateTypeConfigRepository tenantCertificateTypeConfigRepository,
            CertificateTypeFieldRepository certificateTypeFieldRepository) {
        this.certificateTypeRepository = certificateTypeRepository;
        this.tenantCertificateTypeConfigRepository = tenantCertificateTypeConfigRepository;
        this.certificateTypeFieldRepository = certificateTypeFieldRepository;
    }

    /**
     * Reads from the current district shard transaction; the caller must route to the correct shard (e.g. via
     * {@link com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService}).
     */
    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public List<CertificateType> findVisibleCertificateTypesForTenant(UUID tenantId, CertificateTypeCategory category) {
        return certificateTypeRepository.findVisibleCertificateTypesForTenant(tenantId, category);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Map<UUID, TenantCertificateTypeConfig> findTenantCertificateTypeConfigs(
            UUID tenantId,
            List<UUID> certificateTypeIds) {
        if (certificateTypeIds.isEmpty()) {
            return Map.of();
        }
        List<TenantCertificateTypeConfig> rows =
                tenantCertificateTypeConfigRepository.findByTenantIdAndCertificateType_IdIn(tenantId, certificateTypeIds);
        Map<UUID, TenantCertificateTypeConfig> byCertificateTypeId = new HashMap<>(rows.size());
        for (TenantCertificateTypeConfig row : rows) {
            byCertificateTypeId.put(row.getCertificateType().getId(), row);
        }
        return byCertificateTypeId;
    }

    /**
     * Sorted by {@link CertificateTypeField#getSortOrder} within each certificate type id.
     */
    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Map<UUID, List<CertificateTypeField>> findCertificateTypeFieldsByCertificateTypeIds(
            List<UUID> certificateTypeIds) {
        if (certificateTypeIds.isEmpty()) {
            return Map.of();
        }
        List<CertificateTypeField> rows =
                certificateTypeFieldRepository.findByCertificateTypeIdInOrderByCertificateTypeIdAscSortOrderAsc(
                        certificateTypeIds);
        Map<UUID, List<CertificateTypeField>> byTypeId = new LinkedHashMap<>(rows.size());
        for (CertificateTypeField field : rows) {
            byTypeId.computeIfAbsent(field.getCertificateTypeId(), __ -> new ArrayList<>()).add(field);
        }
        return byTypeId;
    }

    private static final int MAX_EXTRA_FIELDS = 40;

    /** Used when {@code icon} is omitted or blank on insert. */
    private static final String DEFAULT_CERTIFICATE_TYPE_ICON = "📜";

    /**
     * Inserts {@code certificate_type} with {@code tenant_id} set to the shard tenant, and optional
     * {@code certificate_type_field} rows.
     */
    @Transactional(transactionManager = "districtTransactionManager")
    public CertificateTypeDto insertTenantCertificateType(TenantShardRoutingContext ctx, CertificateTypeUpsertRequest req) {
        validateInsert(req, ctx.tenantId());
        validateExtraFields(extraFieldRows(req));
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        CertificateType ct = new CertificateType();
        ct.setId(id);
        ct.setTenantId(ctx.tenantId());
        applyTypeFields(ct, req, now);
        certificateTypeRepository.saveAndFlush(ct);
        insertExtraFields(id, extraFieldRows(req));
        return toDtoWithFields(ct);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public List<CertificateType> findTenantOwnedCertificateTypes(UUID tenantId) {
        return certificateTypeRepository.findByTenantIdOrderBySortOrderAscNameMrAsc(tenantId);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<CertificateType> findTenantOwnedCertificateTypeById(UUID tenantId, UUID id) {
        return certificateTypeRepository.findByIdAndTenantId(id, tenantId);
    }

    /**
     * Updates a tenant-owned {@code certificate_type} and replaces {@code certificate_type_field} rows.
     * Catalog {@code code} is not changed.
     */
    @Transactional(transactionManager = "districtTransactionManager")
    public CertificateTypeDto updateTenantCertificateType(
            TenantShardRoutingContext ctx,
            UUID id,
            CertificateTypeUpsertRequest req
    ) {
        CertificateType ct = certificateTypeRepository
                .findByIdAndTenantId(id, ctx.tenantId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tenant certificate type not found."
                ));
        validateExtraFields(extraFieldRows(req));
        Instant now = Instant.now();
        applyMutableTypeFields(ct, req, now);
        certificateTypeRepository.saveAndFlush(ct);
        // delete the existing extra fields and insert the new ones
        certificateTypeFieldRepository.deleteByCertificateTypeId(id);
        insertExtraFields(id, extraFieldRows(req));
        return toDtoWithFields(ct);
    }

    private CertificateTypeDto toDtoWithFields(CertificateType ct) {
        List<CertificateTypeField> fields =
                certificateTypeFieldRepository.findByCertificateTypeIdOrderBySortOrderAsc(ct.getId());
        List<CertificateTypeFieldDto> fieldDtos = fields.stream().map(CertificateTypeFieldDto::fromEntity).toList();
        return CertificateTypeDto.fromCertificateTypeAndTenantConfig(
                ct,
                null,
                ct.getDefaultFeeAmount(),
                fieldDtos
        );
    }

    private static List<CertificateTypeFieldUpsertRequest> extraFieldRows(CertificateTypeUpsertRequest req) {
        return req.extraFields() == null ? List.of() : req.extraFields();
    }

    private void validateInsert(CertificateTypeUpsertRequest req, UUID tenantId) {
        String code = normalizeCode(req.code());
        if (certificateTypeRepository.existsPlatformCertificateTypeWithCodeIgnoreCase(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This code matches a platform certificate type. Choose a different code."
            );
        }
        if (certificateTypeRepository.existsByTenantIdAndCode(tenantId, code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A certificate type with this code already exists for this tenant."
            );
        }
    }

    private void validateExtraFields(List<CertificateTypeFieldUpsertRequest> fields) {
        if (fields.size() > MAX_EXTRA_FIELDS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At most " + MAX_EXTRA_FIELDS + " extra fields are allowed."
            );
        }
        Set<String> seenKeys = new HashSet<>();
        for (CertificateTypeFieldUpsertRequest f : fields) {
            String key = normalizeCode(f.fieldKey());
            if (!seenKeys.add(key)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate field key: " + key);
            }
            if ("SELECT".equals(f.dataType())) {
                JsonNode opts = f.optionsJson();
                if (opts == null || !opts.isArray() || opts.isEmpty()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "SELECT fields require a non-empty optionsJson array."
                    );
                }
            }
        }
    }

    private static void applyMutableTypeFields(CertificateType ct, CertificateTypeUpsertRequest req, Instant now) {
        ct.setCategory(req.category());
        ct.setNameMr(req.nameMr().trim());
        ct.setNameEn(trimToNull(req.nameEn()));
        ct.setDescriptionMr(trimToNull(req.descriptionMr()));
        ct.setDescriptionEn(trimToNull(req.descriptionEn()));
        ct.setExtraFieldsSectionTitleMr(trimToNull(req.extraFieldsSectionTitleMr()));
        ct.setExtraFieldsSectionTitleEn(trimToNull(req.extraFieldsSectionTitleEn()));
        BigDecimal fee = req.defaultFeeAmount() == null ? BigDecimal.ZERO : req.defaultFeeAmount();
        ct.setDefaultFeeAmount(fee);
        ct.setEstimatedDaysTxt(trimToNull(req.estimatedDaysTxt()));
        String icon = trimToNull(req.icon());
        if (icon != null) {
            ct.setIcon(icon);
        }
        ct.setSortOrder(req.sortOrder());
        ct.setActive(req.active());
        ct.setUpdatedAt(now);
    }

    private static void applyTypeFields(CertificateType ct, CertificateTypeUpsertRequest req, Instant now) {
        ct.setCode(normalizeCode(req.code()));
        applyMutableTypeFields(ct, req, now);
        if (trimToNull(req.icon()) == null) {
            ct.setIcon(DEFAULT_CERTIFICATE_TYPE_ICON);
        }
        ct.setCreatedAt(now);
    }

    private void insertExtraFields(UUID certificateTypeId, List<CertificateTypeFieldUpsertRequest> fields) {
        for (CertificateTypeFieldUpsertRequest f : fields) {
            CertificateTypeField row = new CertificateTypeField();
            row.setId(UUID.randomUUID());
            row.setCertificateTypeId(certificateTypeId);
            row.setFieldKey(normalizeCode(f.fieldKey()));
            row.setLabelMr(f.labelMr().trim());
            row.setLabelEn(trimToNull(f.labelEn()));
            row.setPlaceholderMr(trimToNull(f.placeholderMr()));
            row.setPlaceholderEn(trimToNull(f.placeholderEn()));
            row.setHelpTextMr(trimToNull(f.helpTextMr()));
            row.setHelpTextEn(trimToNull(f.helpTextEn()));
            row.setDataType(f.dataType());
            row.setRequired(f.required());
            row.setSortOrder(f.sortOrder());
            row.setOptionsJson(f.optionsJson());
            if ("FILE".equals(f.dataType())) {
                row.setMaxFiles(f.maxFiles() != null ? f.maxFiles().shortValue() : Short.valueOf((short) 1));
                row.setMaxBytes(f.maxBytes() != null ? f.maxBytes() : 5_242_880L);
            } else {
                row.setMaxFiles(null);
                row.setMaxBytes(null);
            }
            certificateTypeFieldRepository.save(row);
        }
    }

    private static String normalizeCode(String code) {
        return code == null ? "" : code.trim().toLowerCase(Locale.ROOT);
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
