package com.asset.smartgrampanchayatapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asset.smartgrampanchayatapi.entity.Tenant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    @EntityGraph(attributePaths = "district")
    Optional<Tenant> findByTenantCode(String tenantCode);
}
