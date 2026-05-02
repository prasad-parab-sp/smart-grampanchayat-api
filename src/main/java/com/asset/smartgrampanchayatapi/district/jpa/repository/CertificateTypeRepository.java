package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateType;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeCategory;

/**
 * Certificate catalog reads use explicit JPQL because the visibility rule is not expressible as a single Spring Data
 * derived query (unlike {@link CitizenRepository}, where methods are simple {@code findBy…} equality / AND lookups).
 * Here we need OR ({@code tenantId} null for platform vs current tenant), null semantics, ordering, and a
 * {@code NOT EXISTS} subquery for platform types disabled in {@code tenant_certificate_type_config}.
 */
public interface CertificateTypeRepository extends JpaRepository<CertificateType, UUID> {

    /**
     * Active types for this tenant: tenant-scoped rows plus platform rows, excluding platform types explicitly
     * disabled via {@link com.asset.smartgrampanchayatapi.district.jpa.model.TenantCertificateTypeConfig}.
     *
     * @param category when non-null, only rows in this catalog group ({@code CERTIFICATE}, {@code REGISTRATION}, etc.)
     */
    @Query(
            """
                    select ct from CertificateType ct
                    where ct.isActive = true
                      and (ct.tenantId is null or ct.tenantId = :tenantId)
                      and (
                          ct.tenantId is not null
                          or not exists (
                              select 1 from TenantCertificateTypeConfig tcc
                              where tcc.certificateType = ct
                                and tcc.tenantId = :tenantId
                                and tcc.enabled = false
                          )
                      )
                      and (:category is null or ct.category = :category)
                    order by ct.sortOrder asc, ct.nameMr asc
                    """
    )
    List<CertificateType> findVisibleCertificateTypesForTenant(
            @Param("tenantId") UUID tenantId,
            @Param("category") CertificateTypeCategory category
    );
}
