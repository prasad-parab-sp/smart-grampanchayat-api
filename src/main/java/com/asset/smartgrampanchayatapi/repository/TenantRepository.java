package com.asset.smartgrampanchayatapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.asset.smartgrampanchayatapi.entity.Tenant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    @Query(
            "SELECT t FROM Tenant t "
                    + "WHERE t.deletedAt IS NULL AND ("
                    + "LOWER(t.tenantCode) = LOWER(:tenantName) OR LOWER(t.name) = LOWER(:tenantName) "
                    + "OR (t.displayName IS NOT NULL AND LOWER(t.displayName) = LOWER(:tenantName)))"
    )
    Optional<Tenant> findActiveByTenantNameOrCodeOrDisplayName(@Param("tenantName") String tenantName);
}
