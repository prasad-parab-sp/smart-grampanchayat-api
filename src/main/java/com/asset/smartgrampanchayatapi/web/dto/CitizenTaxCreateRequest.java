package com.asset.smartgrampanchayatapi.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "CitizenTaxCreateRequest")
public record CitizenTaxCreateRequest(
        @NotNull UUID staffUserId,
        @NotNull UUID taxTypeId,
        @NotBlank @Size(max = 9) String financialYear,
        @Size(max = 64) String assessmentNumber,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amountAssessed,
        @NotNull LocalDate dueDate,
        @Size(max = 4000) String remarks
) {
}
