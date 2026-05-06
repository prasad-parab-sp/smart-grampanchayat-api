package com.asset.smartgrampanchayatapi.web.dto;

import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "CertificateApplicationSubmitRequest", description = "Payload to create a certificate application")
public record CertificateApplicationSubmitRequest(
        @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID certificateTypeId,

        @NotBlank @Size(max = 300) String applicantFullName,
        @NotBlank @Size(max = 15) String applicantMobile,

        @Size(max = 200) String reasonShort,
        String reasonDetails,
        String addressText,
        @Size(max = 300) String forWhomName,

        @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID citizenId,

        @Schema(description = "Dynamic answers keyed by certificate_type_field.field_key (omit or null → {})")
        Map<String, Object> additionalValues
) {
}
