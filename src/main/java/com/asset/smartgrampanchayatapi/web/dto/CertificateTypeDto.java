package com.asset.smartgrampanchayatapi.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateType;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeCategory;
import com.asset.smartgrampanchayatapi.district.jpa.model.TenantCertificateTypeConfig;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * REST payload for one certificate type as listed for a tenant. {@link #feeAmount} is supplied by the application layer
 * (server-side pricing rules), not derived in this record.
 * {@link #tenantCertificateTypeConfig} is present when a {@code tenant_certificate_type_config} row exists.
 */
@Schema(name = "CertificateTypeDto", description = "Certificate type with tenant-specific fee when configured")
public record CertificateTypeDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        UUID tenantId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CertificateTypeCategory category,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nameMr,
        String nameEn,
        String descriptionMr,
        String descriptionEn,
        String extraFieldsSectionTitleMr,
        String extraFieldsSectionTitleEn,
        @Schema(description = "Platform default fee from certificate_type") BigDecimal defaultFeeAmount,
        @Schema(
                description = "Fee for this tenant as computed by the server (e.g. tenant_certificate_type_config or platform default)",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        BigDecimal feeAmount,
        @Schema(description = "Present when this tenant has a tenant_certificate_type_config row for this certificate type")
        TenantCertificateTypeConfigDto tenantCertificateTypeConfig,
        String estimatedDaysTxt,
        String icon,
        int sortOrder,
        @Schema(description = "Alias of is_active") boolean active,
        @Schema(
                description = "Dynamic inputs for apply form; keys match certificate_application.additional_values_json (except FILE)"
        )
        List<CertificateTypeFieldDto> extraFields,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant updatedAt
) {

    /**
     * Maps {@link CertificateType} plus optional {@link TenantCertificateTypeConfig} into this API DTO.
     *
     * @param tenantResolvedFeeAmount fee for the tenant from application-layer rules (never computed here)
     */
    public static CertificateTypeDto fromCertificateTypeAndTenantConfig(
            CertificateType certificateType,
            TenantCertificateTypeConfig tenantCertificateTypeConfig,
            BigDecimal tenantResolvedFeeAmount,
            List<CertificateTypeFieldDto> extraFields
    ) {
        TenantCertificateTypeConfigDto tenantCertificateTypeConfigDto =
                tenantCertificateTypeConfig != null
                        ? TenantCertificateTypeConfigDto.fromTenantCertificateTypeConfig(tenantCertificateTypeConfig)
                        : null;
        return new CertificateTypeDto(
                certificateType.getId(),
                certificateType.getTenantId(),
                certificateType.getCode(),
                certificateType.getCategory(),
                certificateType.getNameMr(),
                certificateType.getNameEn(),
                certificateType.getDescriptionMr(),
                certificateType.getDescriptionEn(),
                certificateType.getExtraFieldsSectionTitleMr(),
                certificateType.getExtraFieldsSectionTitleEn(),
                certificateType.getDefaultFeeAmount(),
                tenantResolvedFeeAmount,
                tenantCertificateTypeConfigDto,
                certificateType.getEstimatedDaysTxt(),
                certificateType.getIcon(),
                certificateType.getSortOrder(),
                certificateType.isActive(),
                extraFields == null ? List.of() : List.copyOf(extraFields),
                certificateType.getCreatedAt(),
                certificateType.getUpdatedAt()
        );
    }
}
