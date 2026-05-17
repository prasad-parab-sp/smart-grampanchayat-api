package com.asset.smartgrampanchayatapi.web.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CitizenRegisterResponse")
public record CitizenRegisterResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<CitizenDto> citizens,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CitizenStatsDto stats
) {
}
