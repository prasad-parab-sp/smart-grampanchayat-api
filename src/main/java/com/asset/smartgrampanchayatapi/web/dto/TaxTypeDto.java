package com.asset.smartgrampanchayatapi.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.asset.smartgrampanchayatapi.district.jpa.model.TaxType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TaxTypeDto")
public record TaxTypeDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nameEn,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nameMr,
        String description,
        boolean active,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant updatedAt
) {
    public static TaxTypeDto fromEntity(TaxType row) {
        return new TaxTypeDto(
                row.getId(),
                row.getNameEn(),
                row.getNameMr(),
                row.getDescription(),
                row.isActive(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }
}
