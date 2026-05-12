package com.asset.smartgrampanchayatapi.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.asset.smartgrampanchayatapi.district.jpa.model.ShardTenant;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * District {@code tenants} row plus officer names: sarpanch from active {@code users} (SARPANCH),
 * gramsevak from {@code grampanchayat.gramsevak_name} when set.
 */
@Schema(name = "TenantProfileDto")
public record TenantProfileDto(
        UUID id,
        String tenantId,
        String tenantCode,
        String name,
        String displayNameEn,
        String displayNameMr,
        String gpCode,
        String districtNameEn,
        String districtNameMr,
        String talukaEn,
        String talukaMr,
        String status,
        String planType,
        LocalDate subscriptionStartDate,
        LocalDate subscriptionEndDate,
        Integer maxUsers,
        String contactEmail,
        String contactPhone,
        String logoUrl,
        String imageUrl,
        Instant createdAt,
        Instant updatedAt,
        @Schema(description = "Active SARPANCH user display name (first + last)")
        String sarpanchName,
        @Schema(description = "From grampanchayat.gramsevak_name when set")
        String gramsevakName
) {

    public static TenantProfileDto fromParts(ShardTenant t, String sarpanchDisplayName, String gramsevakDisplayName) {
        return new TenantProfileDto(
                t.getId(),
                t.getTenantId(),
                t.getTenantCode(),
                t.getName(),
                t.getDisplayNameEn(),
                t.getDisplayNameMr(),
                t.getGpCode(),
                t.getDistrictNameEn(),
                t.getDistrictNameMr(),
                t.getTalukaEn(),
                t.getTalukaMr(),
                t.getStatus(),
                t.getPlanType(),
                t.getSubscriptionStartDate(),
                t.getSubscriptionEndDate(),
                t.getMaxUsers(),
                t.getContactEmail(),
                t.getContactPhone(),
                t.getLogoUrl(),
                t.getImageUrl(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                sarpanchDisplayName,
                gramsevakDisplayName
        );
    }
}
