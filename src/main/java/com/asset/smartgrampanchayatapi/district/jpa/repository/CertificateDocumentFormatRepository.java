package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateDocumentFormat;

public interface CertificateDocumentFormatRepository extends JpaRepository<CertificateDocumentFormat, UUID> {

    List<CertificateDocumentFormat> findByTenantIdOrderByUpdatedAtDesc(UUID tenantId);

    Optional<CertificateDocumentFormat> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<CertificateDocumentFormat> findFirstByTenantIdAndCertificateTypeIdAndActiveTrueOrderByUpdatedAtDesc(
            UUID tenantId,
            UUID certificateTypeId
    );

    boolean existsByTenantIdAndCertificateTypeId(UUID tenantId, UUID certificateTypeId);

    /**
     * Whether any row matches tenant + certificate type with an {@code id} outside {@code excludeIds}.
     * Call with {@code List.of(excludeId)} to mean "any other row than this id" (1:1 duplicate check).
     * <p>
     * Derived-query names {@code ...AndIdNot} / {@code ...AndIdNotEqual} mis-parse for {@code UUID} {@code id}
     * on Spring Data JPA 4; {@code IdNotIn} is the supported spelling.
     */
    boolean existsByTenantIdAndCertificateTypeIdAndIdNotIn(
            UUID tenantId,
            UUID certificateTypeId,
            Collection<UUID> excludeIds
    );
}
