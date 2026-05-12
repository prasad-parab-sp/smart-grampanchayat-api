package com.asset.smartgrampanchayatapi.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.asset.smartgrampanchayatapi.district.jpa.model.DocumentFormatKind;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CertificateDocumentFormatDto", description = "Tenant HTML certificate format for admin and future issuance")
public record CertificateDocumentFormatDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) DocumentFormatKind formatKind,
        UUID certificateTypeId,
        String certificateTypeCode,
        String documentTitle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String bodyHtml,
        String footerNote,
        @Schema(description = "Internal notes for admins (not printed on issued certificates)")
        String internalNote,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean active,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant updatedAt
) {
}
