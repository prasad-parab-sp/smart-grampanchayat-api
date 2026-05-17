package com.asset.smartgrampanchayatapi.web.dto;

public record PlatformStatsDto(
        long districtsTotal,
        long districtsActive,
        long gramPanchayatsTotal,
        long gramPanchayatsActive
) {
}
