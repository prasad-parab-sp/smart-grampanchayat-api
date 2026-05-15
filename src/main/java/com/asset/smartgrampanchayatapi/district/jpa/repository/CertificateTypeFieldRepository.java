package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeField;

public interface CertificateTypeFieldRepository extends JpaRepository<CertificateTypeField, UUID> {

    List<CertificateTypeField> findByCertificateTypeIdInOrderByCertificateTypeIdAscSortOrderAsc(
            Collection<UUID> certificateTypeIds);

    List<CertificateTypeField> findByCertificateTypeIdOrderBySortOrderAsc(UUID certificateTypeId);

    /**
     * Bulk delete so re-inserts in the same transaction do not hit {@code uq_cert_type_field_key}.
     * {@code flushAutomatically} ensures the DELETE hits the database before subsequent inserts.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CertificateTypeField f where f.certificateTypeId = :certificateTypeId")
    void deleteByCertificateTypeId(@Param("certificateTypeId") UUID certificateTypeId);
}
