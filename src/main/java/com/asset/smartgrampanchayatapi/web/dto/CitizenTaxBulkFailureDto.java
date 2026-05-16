package com.asset.smartgrampanchayatapi.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CitizenTaxBulkFailureDto")
public record CitizenTaxBulkFailureDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID citizenId,
        String citizenLabel,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String reason
) {
}
