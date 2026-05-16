package com.asset.smartgrampanchayatapi.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "CitizenTaxWaiveRequest")
public record CitizenTaxWaiveRequest(
        @NotNull UUID staffUserId,
        @Size(max = 4000) String remarks
) {
}
