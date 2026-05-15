package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.util.List;
import java.util.Optional;
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

    /**
     * Same visibility rules as {@link #findVisibleCertificateTypesForTenant} but for a single catalog id.
     */
    @Query(
            """
                    select ct from CertificateType ct
                    where ct.id = :id
                      and ct.isActive = true
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
                    """
    )
    Optional<CertificateType> findVisibleByIdForTenant(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    /**
     * Same visibility rules as {@link #findVisibleByIdForTenant} but by catalog {@code code}.
     */
    @Query(
            """
                    select ct from CertificateType ct
                    where ct.code = :code
                      and ct.isActive = true
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
                    """
    )
    Optional<CertificateType> findVisibleByCodeForTenant(
            @Param("code") String code,
            @Param("tenantId") UUID tenantId
    );

    @Query(
            """
                    select case when count(ct) > 0 then true else false end
                    from CertificateType ct
                    where ct.tenantId is null and lower(trim(ct.code)) = lower(trim(:code))
                    """
    )
    boolean existsPlatformCertificateTypeWithCodeIgnoreCase(@Param("code") String code);

    boolean existsByTenantIdAndCode(UUID tenantId, String code);

    Optional<CertificateType> findByIdAndTenantId(UUID id, UUID tenantId);

    List<CertificateType> findByTenantIdOrderBySortOrderAscNameMrAsc(UUID tenantId);
}
