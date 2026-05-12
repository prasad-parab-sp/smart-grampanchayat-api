package com.asset.smartgrampanchayatapi.web.dto;

import com.asset.smartgrampanchayatapi.district.jpa.model.DocumentFormatKind;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "CertificateDocumentFormatUpsertRequest")
public record CertificateDocumentFormatUpsertRequest(
        @NotBlank
        @Size(max = 300)
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Display name in admin list")
        String name,
        @NotNull
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        DocumentFormatKind formatKind,
        @Schema(description = "When set, must be a certificate type code visible to this tenant")
        String certificateTypeCode,
        @Size(max = 500)
        String documentTitle,
        @NotBlank
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String bodyHtml,
        String footerNote,
        @Schema(description = "Internal notes for admins (not printed on issued certificates)")
        String internalNote,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "When false, hidden from default issuance picks")
        boolean active
) {
}
