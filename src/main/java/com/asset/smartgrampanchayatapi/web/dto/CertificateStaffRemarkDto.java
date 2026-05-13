package com.asset.smartgrampanchayatapi.web.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CertificateStaffRemarkDto", description = "One remark line from Gramsevak staff")
public record CertificateStaffRemarkDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID createdByUserId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 4000) String text
) {
}
