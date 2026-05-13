package com.asset.smartgrampanchayatapi.web.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Gramsevak credentials to reject an application (same optional remark lines as approve).
 */
@Schema(name = "CertificateApplicationRejectRequest")
public record CertificateApplicationRejectRequest(
        @NotBlank @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "9405715871") String identifier,
        @NotBlank @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String password,
        @Size(max = 20) @Schema(description = "Optional: each non-blank line is appended as a staff remark before rejection.")
        List<@NotBlank @Size(max = 4000) String> remarksToAppend
) {
    public CertificateApplicationRejectRequest {
        remarksToAppend = remarksToAppend == null ? List.of() : List.copyOf(remarksToAppend);
    }
}
