package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asset.smartgrampanchayatapi.district.jpa.model.Grampanchayat;

@Repository
public interface GrampanchayatRepository extends JpaRepository<Grampanchayat, UUID> {

    Optional<Grampanchayat> findByTenantId(UUID tenantId);

    Optional<Grampanchayat> findByGpCode(String gpCode);

    List<Grampanchayat> findAllByTenantId(UUID tenantId);
}
