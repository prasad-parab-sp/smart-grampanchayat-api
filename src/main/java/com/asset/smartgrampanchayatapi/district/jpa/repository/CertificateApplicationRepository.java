package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateApplication;

public interface CertificateApplicationRepository extends JpaRepository<CertificateApplication, UUID>,
        JpaSpecificationExecutor<CertificateApplication> {

    Optional<CertificateApplication> findByIdAndTenantId(UUID id, UUID tenantId);

    /** All-time application count for this tenant — next display sequence is {@code count + 1} (no yearly reset). */
    long countByTenantId(UUID tenantId);
}
