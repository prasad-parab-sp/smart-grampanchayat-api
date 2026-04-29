package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asset.smartgrampanchayatapi.district.jpa.model.ShardTenant;

@Repository
public interface ShardTenantRepository extends JpaRepository<ShardTenant, UUID> {

    Optional<ShardTenant> findByTenantCode(String tenantCode);
}
