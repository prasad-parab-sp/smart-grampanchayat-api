package com.asset.smartgrampanchayatapi.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Wraps {@link CertificateTypeUpsertRequest} with staff credentials (same model as certificate approval until JWT exists).
 */
@Schema(name = "CertificateTypeCreateRequest")
public record CertificateTypeCreateRequest(
        @NotBlank
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "9405715871")
        String identifier,
        @NotBlank
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String password,
        @NotNull
        @Valid
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        CertificateTypeUpsertRequest certificateType
) {
}
