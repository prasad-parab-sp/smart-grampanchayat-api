package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateDocumentFormat;

public interface CertificateDocumentFormatRepository extends JpaRepository<CertificateDocumentFormat, UUID> {

    List<CertificateDocumentFormat> findByTenantIdOrderByUpdatedAtDesc(UUID tenantId);

    Optional<CertificateDocumentFormat> findByIdAndTenantId(UUID id, UUID tenantId);
}
