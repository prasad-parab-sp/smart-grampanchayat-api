package com.asset.smartgrampanchayatapi.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.asset.smartgrampanchayatapi.district.jpa.model.CitizenTax;
import com.asset.smartgrampanchayatapi.district.jpa.model.CitizenTaxStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CitizenTaxDto")
public record CitizenTaxDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID citizenId,
        String citizenFirstName,
        String citizenLastName,
        String citizenMobile,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID taxTypeId,
        String taxTypeNameEn,
        String taxTypeNameMr,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String financialYear,
        String assessmentNumber,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal amountAssessed,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal amountOutstanding,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate dueDate,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CitizenTaxStatus status,
        String remarks,
        UUID createdByUserId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant updatedAt
) {
    public static CitizenTaxDto fromEntity(
            CitizenTax row,
            TaxTypeDto taxType,
            String citizenFirstName,
            String citizenLastName,
            String citizenMobile
    ) {
        return new CitizenTaxDto(
                row.getId(),
                row.getCitizenId(),
                citizenFirstName,
                citizenLastName,
                citizenMobile,
                row.getTaxTypeId(),
                taxType != null ? taxType.nameEn() : null,
                taxType != null ? taxType.nameMr() : null,
                row.getFinancialYear(),
                row.getAssessmentNumber(),
                row.getAmountAssessed(),
                row.getAmountOutstanding(),
                row.getDueDate(),
                row.getStatus(),
                row.getRemarks(),
                row.getCreatedByUserId(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }
}
