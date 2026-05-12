package com.asset.smartgrampanchayatapi.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Gramsevak must re-confirm credentials to approve (no JWT yet). Identifier is mobile or email as used at login.
 */
@Schema(name = "CertificateApplicationApproveRequest")
public record CertificateApplicationApproveRequest(
        @NotBlank @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "9405715871") String identifier,
        @NotBlank @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String password
) {
}
