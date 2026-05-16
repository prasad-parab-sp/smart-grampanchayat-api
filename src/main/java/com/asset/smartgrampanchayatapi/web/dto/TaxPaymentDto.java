package com.asset.smartgrampanchayatapi.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.asset.smartgrampanchayatapi.district.jpa.model.TaxPayment;
import com.asset.smartgrampanchayatapi.district.jpa.model.TaxPaymentMode;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TaxPaymentDto")
public record TaxPaymentDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID citizenTaxId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal amount,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate paidOn,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TaxPaymentMode paymentMode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String receiptNumber,
        String reference,
        UUID recordedByUserId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt
) {
    public static TaxPaymentDto fromEntity(TaxPayment row) {
        return new TaxPaymentDto(
                row.getId(),
                row.getCitizenTaxId(),
                row.getAmount(),
                row.getPaidOn(),
                row.getPaymentMode(),
                row.getReceiptNumber(),
                row.getReference(),
                row.getRecordedByUserId(),
                row.getCreatedAt()
        );
    }
}
