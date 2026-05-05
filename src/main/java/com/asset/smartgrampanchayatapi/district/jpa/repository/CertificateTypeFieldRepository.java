package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeField;

public interface CertificateTypeFieldRepository extends JpaRepository<CertificateTypeField, UUID> {

    List<CertificateTypeField> findByCertificateTypeIdInOrderByCertificateTypeIdAscSortOrderAsc(
            Collection<UUID> certificateTypeIds);
}
