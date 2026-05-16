package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asset.smartgrampanchayatapi.district.jpa.model.CitizenTax;
import com.asset.smartgrampanchayatapi.district.jpa.model.CitizenTaxStatus;

public interface CitizenTaxRepository extends JpaRepository<CitizenTax, UUID> {

    List<CitizenTax> findByTenantIdAndCitizenIdOrderByDueDateDescCreatedAtDesc(UUID tenantId, UUID citizenId);

    Optional<CitizenTax> findByTenantIdAndId(UUID tenantId, UUID id);

    boolean existsByCitizenIdAndTaxTypeIdAndFinancialYearAndStatusNot(
            UUID citizenId,
            UUID taxTypeId,
            String financialYear,
            CitizenTaxStatus status
    );

    @Query("""
            SELECT t FROM CitizenTax t
            WHERE t.tenantId = :tenantId
              AND (:status IS NULL OR t.status = :status)
              AND (:financialYear IS NULL OR t.financialYear = :financialYear)
            ORDER BY t.dueDate ASC, t.createdAt DESC
            """)
    List<CitizenTax> findByTenantForList(
            @Param("tenantId") UUID tenantId,
            @Param("status") CitizenTaxStatus status,
            @Param("financialYear") String financialYear
    );
}
