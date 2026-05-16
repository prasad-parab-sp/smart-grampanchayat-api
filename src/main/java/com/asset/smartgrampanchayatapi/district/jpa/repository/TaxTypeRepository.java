package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asset.smartgrampanchayatapi.district.jpa.model.TaxType;

public interface TaxTypeRepository extends JpaRepository<TaxType, UUID> {

    List<TaxType> findByTenantIdOrderByNameEnAsc(UUID tenantId);

    List<TaxType> findByTenantIdAndActiveTrueOrderByNameEnAsc(UUID tenantId);

    Optional<TaxType> findByTenantIdAndId(UUID tenantId, UUID id);
}
