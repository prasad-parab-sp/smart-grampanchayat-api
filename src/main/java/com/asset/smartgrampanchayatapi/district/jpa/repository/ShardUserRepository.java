package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asset.smartgrampanchayatapi.district.jpa.model.ShardUser;
import com.asset.smartgrampanchayatapi.district.jpa.model.UserRole;

@Repository
public interface ShardUserRepository extends JpaRepository<ShardUser, UUID> {

    Optional<ShardUser> findByTenantIdAndEmail(UUID tenantId, String email);

    Optional<ShardUser> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);

    Optional<ShardUser> findByTenantIdAndPhone(UUID tenantId, String phone);

    List<ShardUser> findAllByTenantId(UUID tenantId);

    List<ShardUser> findAllByTenantIdAndRoleIn(UUID tenantId, List<UserRole> roles);

    /** At most one row expected when partial unique index {@code uq_users_one_active_sarpanch_per_tenant} is present. */
    Optional<ShardUser> findByTenantIdAndRoleAndActiveTrue(UUID tenantId, UserRole role);

    Optional<ShardUser> findByTenantIdAndId(UUID tenantId, UUID id);

}
