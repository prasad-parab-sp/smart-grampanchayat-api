package com.asset.smartgrampanchayatapi.web.dto;

import java.math.BigDecimal;
import java.util.List;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeCategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "CertificateTypeUpsertRequest", description = "Insert a tenant-owned row into certificate_type (district shard)")
public record CertificateTypeUpsertRequest(
        @NotBlank
        @Size(min = 2, max = 64)
        @Pattern(regexp = "[a-z][a-z0-9_]*", message = "code must start with a letter and use lowercase snake_case")
        @Schema(example = "custom_noc")
        String code,
        @NotNull CertificateTypeCategory category,
        @NotBlank
        @Size(max = 300)
        String nameMr,
        @NotBlank
        @Size(max = 300)
        String nameEn,
        @NotBlank
        @Size(max = 4000)
        String descriptionMr,
        @NotBlank
        @Size(max = 4000)
        String descriptionEn,
        @NotBlank
        @Size(max = 300)
        String extraFieldsSectionTitleMr,
        @NotBlank
        @Size(max = 300)
        String extraFieldsSectionTitleEn,
        @NotNull
        @DecimalMin("0.0")
        BigDecimal defaultFeeAmount,
        @NotBlank
        @Size(max = 80)
        String estimatedDaysTxt,
        @Size(max = 32)
        String icon,
        int sortOrder,
        @Schema(description = "When false, the type is hidden from the citizen catalog")
        boolean active,
        @Valid
        @Schema(description = "Optional extra form fields (certificate_type_field); omit or use [] for none")
        List<CertificateTypeFieldUpsertRequest> extraFields
) {
}
