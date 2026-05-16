package com.asset.smartgrampanchayatapi.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "TaxTypeUpsertRequest")
public record TaxTypeUpsertRequest(
        @NotBlank @Size(max = 200) String nameEn,
        @NotBlank @Size(max = 200) String nameMr,
        @Size(max = 4000) String description,
        Boolean active
) {
}
