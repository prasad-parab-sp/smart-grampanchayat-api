package com.asset.smartgrampanchayatapi.web.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Provisions a gram panchayat tenant on the master routing DB and the target district shard.
 */
@Schema(name = "TenantCreateRequest")
public record TenantCreateRequest(
        @NotBlank @Size(max = 20) String districtCode,
        @NotBlank @Size(max = 10) String tenantCode,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 20) String tenantId,
        @NotBlank @Size(max = 64) String gpCode,
        @Size(max = 255) String displayNameEn,
        @Size(max = 255) String displayNameMr,
        @Size(max = 255) String talukaEn,
        @Size(max = 255) String talukaMr,
        @Size(max = 32) String status,
        @Size(max = 32) String planType,
        LocalDate subscriptionStartDate,
        LocalDate subscriptionEndDate,
        Integer maxUsers,
        @Size(max = 255) String contactEmail,
        @Size(max = 64) String contactPhone,
        String logoUrl,
        String imageUrl
) {

    public String resolvedStatus() {
        return status == null || status.isBlank() ? "trial" : status.trim();
    }

    public String resolvedPlanType() {
        return planType == null || planType.isBlank() ? "basic" : planType.trim();
    }
}
