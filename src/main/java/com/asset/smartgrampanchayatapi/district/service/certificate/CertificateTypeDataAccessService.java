package com.asset.smartgrampanchayatapi.district.service.certificate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateType;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeCategory;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeField;
import com.asset.smartgrampanchayatapi.district.jpa.model.TenantCertificateTypeConfig;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CertificateTypeFieldRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CertificateTypeRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.TenantCertificateTypeConfigRepository;

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
}
