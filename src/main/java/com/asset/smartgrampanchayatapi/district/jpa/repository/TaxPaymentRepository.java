package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asset.smartgrampanchayatapi.district.jpa.model.TaxPayment;

public interface TaxPaymentRepository extends JpaRepository<TaxPayment, UUID> {

    List<TaxPayment> findByCitizenTaxIdOrderByPaidOnDescCreatedAtDesc(UUID citizenTaxId);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM TaxPayment p
            WHERE p.citizenTaxId = :citizenTaxId
            """)
    BigDecimal sumAmountByCitizenTaxId(@Param("citizenTaxId") UUID citizenTaxId);
}
