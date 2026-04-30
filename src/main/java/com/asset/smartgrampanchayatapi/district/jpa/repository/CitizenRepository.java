package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asset.smartgrampanchayatapi.district.jpa.model.Citizen;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, UUID> {

    Optional<Citizen> findByCitizenUid(String citizenUid);

    List<Citizen> findAllByTenantId(UUID tenantId);

    List<Citizen> findAllByGrampanchayatId(UUID grampanchayatId);

    List<Citizen> findAllByTenantIdAndGrampanchayatId(UUID tenantId, UUID grampanchayatId);

    /** Expects at most one row per {@code (tenantId, mobile)} — enforce with DB unique constraint. */
    Optional<Citizen> findByTenantIdAndMobile(UUID tenantId, String mobile);

    Optional<Citizen> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);

    Optional<Citizen> findByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * Stricter scope when mobile is unique per GP; enforce with
     * {@code UNIQUE (tenant_id, grampanchayat_id, mobile)} if that is your rule.
     */
    Optional<Citizen> findByTenantIdAndGrampanchayatIdAndMobile(
            UUID tenantId, UUID grampanchayatId, String mobile);
}
