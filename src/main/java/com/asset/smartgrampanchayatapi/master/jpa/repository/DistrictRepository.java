package com.asset.smartgrampanchayatapi.master.jpa.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asset.smartgrampanchayatapi.master.jpa.model.District;

@Repository
public interface DistrictRepository extends JpaRepository<District, UUID> {

    Optional<District> findByDistrictCode(String districtCode);

    long count();

    long countByStatusIgnoreCase(String status);
}
