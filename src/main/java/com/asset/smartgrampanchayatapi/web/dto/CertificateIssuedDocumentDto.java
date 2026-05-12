package com.asset.smartgrampanchayatapi.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CertificateIssuedDocumentDto", description = "Printable HTML certificate for an approved application")
public record CertificateIssuedDocumentDto(
        @Schema(description = "Human-readable application / certificate number") String applicationNumber,
        @Schema(description = "Trusted HTML fragment (wrap with print CSS on the client)") String html
) {
}
