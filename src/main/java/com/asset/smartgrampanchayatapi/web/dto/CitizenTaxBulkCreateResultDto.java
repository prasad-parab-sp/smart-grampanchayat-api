package com.asset.smartgrampanchayatapi.web.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CitizenTaxBulkCreateResultDto")
public record CitizenTaxBulkCreateResultDto(
        int createdCount,
        int failedCount,
        List<CitizenTaxDto> created,
        List<CitizenTaxBulkFailureDto> failures
) {
}
