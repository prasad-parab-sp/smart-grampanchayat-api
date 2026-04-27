package com.asset.smartgrampanchayatapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asset.smartgrampanchayatapi.entity.MasterTenant;

@Repository
public interface MasterTenantRepository extends JpaRepository<MasterTenant, UUID> {

    @EntityGraph(attributePaths = "district")
    Optional<MasterTenant> findByTenantCode(String tenantCode);
}
