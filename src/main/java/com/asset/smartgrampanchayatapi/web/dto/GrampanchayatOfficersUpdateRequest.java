package com.asset.smartgrampanchayatapi.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Partial update for gramsevak display name on {@code grampanchayat}. Sarpanch comes from {@code users} (role SARPANCH).
 */
@Schema(name = "GrampanchayatOfficersUpdateRequest")
public record GrampanchayatOfficersUpdateRequest(
        @Size(max = 300)
        @Schema(description = "Omit or null to leave unchanged. Empty string clears.")
        String gramsevakName
) {
}
