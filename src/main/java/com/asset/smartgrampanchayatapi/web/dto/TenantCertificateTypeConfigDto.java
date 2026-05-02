package com.asset.smartgrampanchayatapi.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.asset.smartgrampanchayatapi.district.jpa.model.TenantCertificateTypeConfig;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Tenant-specific row from {@code tenant_certificate_type_config} embedded on {@link CertificateTypeDto}.
 */
@Schema(name = "TenantCertificateTypeConfigDto", description = "Per-tenant fee and visibility for a certificate type")
public record TenantCertificateTypeConfigDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID tenantId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal feeAmount,
        @Schema(description = "Whether this certificate type is offered for the tenant", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean enabled,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant updatedAt
) {

    public static TenantCertificateTypeConfigDto fromTenantCertificateTypeConfig(TenantCertificateTypeConfig entity) {
        return new TenantCertificateTypeConfigDto(
                entity.getId(),
                entity.getTenantId(),
                entity.getFeeAmount(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
