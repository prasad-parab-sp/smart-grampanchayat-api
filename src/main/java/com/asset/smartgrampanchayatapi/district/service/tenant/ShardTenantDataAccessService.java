package com.asset.smartgrampanchayatapi.district.service.tenant;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asset.smartgrampanchayatapi.district.jpa.model.PlanType;
import com.asset.smartgrampanchayatapi.district.jpa.model.ShardTenant;
import com.asset.smartgrampanchayatapi.district.jpa.model.TenantStatus;
import com.asset.smartgrampanchayatapi.district.jpa.repository.ShardTenantRepository;
import com.asset.smartgrampanchayatapi.master.jpa.model.District;
import com.asset.smartgrampanchayatapi.web.dto.TenantCreateRequest;

@Service
public class ShardTenantDataAccessService {

    private final ShardTenantRepository shardTenantRepository;

    public ShardTenantDataAccessService(ShardTenantRepository shardTenantRepository) {
        this.shardTenantRepository = shardTenantRepository;
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<ShardTenant> findByTenantCode(String tenantCode) {
        return shardTenantRepository.findByTenantCode(tenantCode);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public boolean existsByTenantCode(String tenantCode) {
        return shardTenantRepository.existsByTenantCode(tenantCode);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public boolean existsByGpCode(String gpCode) {
        return shardTenantRepository.existsByGpCode(gpCode);
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public ShardTenant insertTenant(UUID id, District district, TenantCreateRequest body, String tenantCode, String tenantId) {
        Instant now = Instant.now();
        ShardTenant row = ShardTenant.newRow();
        row.setId(id);
        row.setTenantId(tenantId);
        row.setTenantCode(tenantCode);
        row.setName(body.name().trim());
        row.setDisplayNameEn(trimToNull(body.displayNameEn()));
        row.setDisplayNameMr(trimToNull(body.displayNameMr()));
        row.setGpCode(body.gpCode().trim());
        row.setDistrictNameEn(coalesceDisplay(district.getDisplayNameEn(), district.getName()));
        row.setDistrictNameMr(coalesceDisplay(district.getDisplayNameMr(), district.getName()));
        row.setTalukaEn(trimToNull(body.talukaEn()));
        row.setTalukaMr(trimToNull(body.talukaMr()));
        row.setStatus(TenantStatus.fromApiValue(body.resolvedStatus()));
        row.setPlanType(PlanType.fromApiValue(body.resolvedPlanType()));
        row.setSubscriptionStartDate(body.subscriptionStartDate());
        row.setSubscriptionEndDate(body.subscriptionEndDate());
        row.setMaxUsers(body.maxUsers());
        row.setContactEmail(trimToNull(body.contactEmail()));
        row.setContactPhone(trimToNull(body.contactPhone()));
        row.setLogoUrl(trimToNull(body.logoUrl()));
        row.setImageUrl(trimToNull(body.imageUrl()));
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        return shardTenantRepository.save(row);
    }

    private static String coalesceDisplay(String preferred, String fallback) {
        String p = trimToNull(preferred);
        if (p != null) {
            return p;
        }
        return fallback == null ? null : fallback.trim();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
