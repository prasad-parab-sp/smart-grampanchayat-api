package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asset.smartgrampanchayatapi.district.jpa.model.TenantCertificateTypeConfig;

public interface TenantCertificateTypeConfigRepository extends JpaRepository<TenantCertificateTypeConfig, UUID> {

    /**
     * Full {@code tenant_certificate_type_config} rows for the given catalog type ids.
     */
    List<TenantCertificateTypeConfig> findByTenantIdAndCertificateType_IdIn(
            UUID tenantId,
            Collection<UUID> certificateTypeIds
    );
}
