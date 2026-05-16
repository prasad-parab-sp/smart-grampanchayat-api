package com.asset.smartgrampanchayatapi.district.jpa.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asset.smartgrampanchayatapi.district.jpa.model.GpNotice;
import com.asset.smartgrampanchayatapi.district.jpa.model.NoticeType;

public interface GpNoticeRepository extends JpaRepository<GpNotice, UUID> {

    @Query("""
            SELECT n FROM GpNotice n
            WHERE n.tenantId = :tenantId
              AND (:includeExpired = true OR n.expiresOn >= :today)
            ORDER BY n.publishedOn DESC, n.createdAt DESC
            """)
    List<GpNotice> findByTenantForList(
            @Param("tenantId") UUID tenantId,
            @Param("today") LocalDate today,
            @Param("includeExpired") boolean includeExpired
    );

    @Query("""
            SELECT n FROM GpNotice n
            WHERE n.tenantId = :tenantId
              AND n.noticeType = :noticeType
              AND (:includeExpired = true OR n.expiresOn >= :today)
            ORDER BY n.publishedOn DESC, n.createdAt DESC
            """)
    List<GpNotice> findByTenantAndTypeForList(
            @Param("tenantId") UUID tenantId,
            @Param("noticeType") NoticeType noticeType,
            @Param("today") LocalDate today,
            @Param("includeExpired") boolean includeExpired
    );

    Optional<GpNotice> findByTenantIdAndId(UUID tenantId, UUID id);
}
