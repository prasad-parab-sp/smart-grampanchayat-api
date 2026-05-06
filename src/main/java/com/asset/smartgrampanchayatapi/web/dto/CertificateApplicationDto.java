package com.asset.smartgrampanchayatapi.web.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateApplication;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateApplicationStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CertificateApplicationDto", description = "Stored certificate application with dynamic field values")
public record CertificateApplicationDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID tenantId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID certificateTypeId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String applicationNumber,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String applicantFullName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String applicantMobile,
        String reasonShort,
        String reasonDetails,
        String addressText,
        String forWhomName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID citizenId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CertificateApplicationStatus status,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant submittedAt,
        Instant paidAt,
        String paymentReference,
        @Schema(description = "Keyed by certificate_type_field.field_key (JSON object from DB via ObjectMapper)")
        Map<String, Object> additionalValues
) {
    public static CertificateApplicationDto fromEntity(CertificateApplication e, ObjectMapper objectMapper) {
        Map<String, Object> values = jsonNodeToMap(e.getAdditionalValuesJson(), objectMapper);
        return new CertificateApplicationDto(
                e.getId(),
                e.getTenantId(),
                e.getCertificateTypeId(),
                e.getApplicationNumber(),
                e.getApplicantFullName(),
                e.getApplicantMobile(),
                e.getReasonShort(),
                e.getReasonDetails(),
                e.getAddressText(),
                e.getForWhomName(),
                e.getCitizenId(),
                e.getStatus(),
                e.getSubmittedAt(),
                e.getPaidAt(),
                e.getPaymentReference(),
                values
        );
    }

    private static final TypeReference<Map<String, Object>> MAP_STRING_OBJECT = new TypeReference<>() {};

    private static Map<String, Object> jsonNodeToMap(JsonNode node, ObjectMapper objectMapper) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return Map.of();
        }
        return objectMapper.convertValue(node, MAP_STRING_OBJECT);
    }
}
