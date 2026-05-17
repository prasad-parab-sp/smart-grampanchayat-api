package com.asset.smartgrampanchayatapi.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CitizenStatsDto")
public record CitizenStatsDto(
        int totalActive,
        int voters,
        int male,
        int female,
        int bpl,
        int disabled,
        int deceased,
        int migrated
) {
}
