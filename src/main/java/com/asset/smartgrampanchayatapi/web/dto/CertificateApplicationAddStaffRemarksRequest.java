package com.asset.smartgrampanchayatapi.web.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(name = "CertificateApplicationAddStaffRemarksRequest")
public record CertificateApplicationAddStaffRemarksRequest(
        @NotBlank @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "9405715871") String identifier,
        @NotBlank @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String password,
        @NotEmpty @Size(max = 20) @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<@NotBlank @Size(max = 4000) String> texts
) {
}
