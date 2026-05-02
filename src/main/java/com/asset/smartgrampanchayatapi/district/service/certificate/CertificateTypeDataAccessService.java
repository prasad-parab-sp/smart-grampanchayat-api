package com.asset.smartgrampanchayatapi.district.service.certificate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateType;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeCategory;
import com.asset.smartgrampanchayatapi.district.jpa.model.TenantCertificateTypeConfig;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CertificateTypeRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.TenantCertificateTypeConfigRepository;

@Service
public class CertificateTypeDataAccessService {

    private final CertificateTypeRepository certificateTypeRepository;
    private final TenantCertificateTypeConfigRepository tenantCertificateTypeConfigRepository;

    public CertificateTypeDataAccessService(
            CertificateTypeRepository certificateTypeRepository,
            TenantCertificateTypeConfigRepository tenantCertificateTypeConfigRepository) {
        this.certificateTypeRepository = certificateTypeRepository;
        this.tenantCertificateTypeConfigRepository = tenantCertificateTypeConfigRepository;
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
}
