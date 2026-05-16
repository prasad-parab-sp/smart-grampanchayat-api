package com.asset.smartgrampanchayatapi.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.asset.smartgrampanchayatapi.district.jpa.model.TaxPaymentMode;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "TaxPaymentCreateRequest")
public record TaxPaymentCreateRequest(
        @NotNull UUID staffUserId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        @NotNull LocalDate paidOn,
        @NotNull TaxPaymentMode paymentMode,
        @Size(max = 100) String reference
) {
}
